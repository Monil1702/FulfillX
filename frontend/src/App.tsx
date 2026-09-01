import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { io } from "socket.io-client";
import { api, urls } from "./api";
import type { CreateOrder, InventoryItem, Notification, Order, OrderStatus } from "./types";

const money = new Intl.NumberFormat("en-CA", { style: "currency", currency: "CAD" });

const statusLabel: Record<OrderStatus, string> = {
  PENDING_INVENTORY: "Pending inventory",
  PRIORITY_PENDING: "Priority pending",
  CONFIRMED: "Confirmed",
  REJECTED: "Rejected"
};

const initialOrder: CreateOrder = {
  customerEmail: "buyer@example.com",
  sku: "LAPTOP-PRO",
  quantity: 1,
  unitPrice: 1499,
  priority: false
};

export default function App() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [inventory, setInventory] = useState<InventoryItem[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [form, setForm] = useState<CreateOrder>(initialOrder);
  const [submitting, setSubmitting] = useState(false);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState("");

  const refresh = useCallback(async () => {
    try {
      const [nextOrders, nextInventory, nextNotifications] = await Promise.all([
        api.listOrders(), api.listInventory(), api.listNotifications()
      ]);
      setOrders(nextOrders);
      setInventory(nextInventory);
      setNotifications(nextNotifications);
      setError("");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Services are unavailable");
    }
  }, []);

  useEffect(() => {
    void refresh();
    const timer = window.setInterval(() => void refresh(), 5000);
    return () => window.clearInterval(timer);
  }, [refresh]);

  useEffect(() => {
    const socket = io(urls.notifications, { transports: ["websocket", "polling"] });
    socket.on("connect", () => setConnected(true));
    socket.on("disconnect", () => setConnected(false));
    socket.on("notification", (notification: Notification) => {
      setNotifications((current) => [notification, ...current].slice(0, 100));
      void refresh();
    });
    return () => {
      socket.disconnect();
    };
  }, [refresh]);

  const stats = useMemo(() => ({
    orders: orders.length,
    confirmed: orders.filter((order) => order.status === "CONFIRMED").length,
    pending: orders.filter((order) => order.status.includes("PENDING")).length,
    value: orders.reduce((sum, order) => sum + Number(order.total), 0)
  }), [orders]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      await api.createOrder(form);
      setForm((current) => ({ ...current, quantity: 1 }));
      await refresh();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Could not place order");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#top" aria-label="FulfillX home">
          <span className="brand-mark">FX</span>
          <span>FulfillX</span>
        </a>
        <nav aria-label="Primary navigation">
          <a className="nav-item active" href="#overview">Overview</a>
          <a className="nav-item" href="#orders">Orders</a>
          <a className="nav-item" href="#inventory">Inventory</a>
          <a className="nav-item" href="#events">Event stream</a>
        </nav>
        <div className="sidebar-card">
          <span className={`pulse ${connected ? "online" : ""}`} />
          <div>
            <strong>{connected ? "Live stream active" : "Connecting…"}</strong>
            <small>RabbitMQ → Socket.IO</small>
          </div>
        </div>
      </aside>

      <main id="top">
        <header className="topbar">
          <div>
            <p className="eyebrow">OPERATIONS CONTROL</p>
            <h1>Order fulfillment, in real time.</h1>
          </div>
          <div className="environment"><span /> Development</div>
        </header>

        {error && <div className="error-banner" role="alert">{error}. Start the services and retry.</div>}

        <section className="metrics" id="overview" aria-label="Order metrics">
          <Metric label="Total orders" value={stats.orders.toString()} detail="All-time volume" />
          <Metric label="Confirmed" value={stats.confirmed.toString()} detail="Inventory reserved" tone="good" />
          <Metric label="In progress" value={stats.pending.toString()} detail="Event processing" tone="warn" />
          <Metric label="Order value" value={money.format(stats.value)} detail="Gross merchandise value" />
        </section>

        <section className="content-grid">
          <div className="panel order-panel" id="orders">
            <div className="panel-heading">
              <div><p className="eyebrow">NEW WORKFLOW</p><h2>Place an order</h2></div>
              <span className="architecture-chip">Transactional outbox</span>
            </div>
            <form onSubmit={submit} data-testid="order-form">
              <label>Email<input type="email" value={form.customerEmail} onChange={(event) => setForm({ ...form, customerEmail: event.target.value })} required /></label>
              <label>Product
                <select value={form.sku} onChange={(event) => setForm({ ...form, sku: event.target.value })}>
                  {inventory.map((item) => <option value={item.sku} key={item.sku}>{item.name} · {item.available} available</option>)}
                  {!inventory.length && <option value="LAPTOP-PRO">Developer Laptop</option>}
                </select>
              </label>
              <div className="form-row">
                <label>Quantity<input type="number" min="1" value={form.quantity} onChange={(event) => setForm({ ...form, quantity: Number(event.target.value) })} required /></label>
                <label>Unit price<input type="number" min="0.01" step="0.01" value={form.unitPrice} onChange={(event) => setForm({ ...form, unitPrice: Number(event.target.value) })} required /></label>
              </div>
              <label className="switch-row">
                <input type="checkbox" checked={form.priority} onChange={(event) => setForm({ ...form, priority: event.target.checked })} />
                <span className="switch" />
                <span><strong>Priority fulfillment</strong><small>Routes through the priority Strategy</small></span>
              </label>
              <button type="submit" disabled={submitting}>{submitting ? "Publishing event…" : "Place order"}<span>→</span></button>
            </form>
          </div>

          <div className="panel event-panel" id="events">
            <div className="panel-heading">
              <div><p className="eyebrow">LIVE EVENTS</p><h2>Notification stream</h2></div>
              <span className={`connection-dot ${connected ? "connected" : ""}`} title="WebSocket status" />
            </div>
            <div className="event-list" aria-live="polite">
              {notifications.map((notification) => (
                <article className="event" key={notification.id} data-testid="notification">
                  <span className={`event-icon ${notification.outcome}`}>{notification.outcome === "confirmed" ? "✓" : "!"}</span>
                  <div><strong>Order {notification.outcome}</strong><p>{notification.message}</p><small>{notification.orderId.slice(0, 8)} · {new Date(notification.receivedAt).toLocaleTimeString()}</small></div>
                </article>
              ))}
              {!notifications.length && <div className="empty-state"><span>↯</span><p>Events will appear as orders move through Kafka and RabbitMQ.</p></div>}
            </div>
          </div>
        </section>

        <section className="panel table-panel">
          <div className="panel-heading"><div><p className="eyebrow">ORDER LEDGER</p><h2>Recent orders</h2></div><button className="text-button" onClick={() => void refresh()}>Refresh</button></div>
          <div className="table-scroll">
            <table>
              <thead><tr><th>Order</th><th>Customer</th><th>Item</th><th>Total</th><th>Policy</th><th>Status</th></tr></thead>
              <tbody>
                {orders.map((order) => <tr key={order.id} data-testid="order-row"><td><strong>#{order.id.slice(0, 8)}</strong><small>{new Date(order.createdAt).toLocaleString()}</small></td><td>{order.customerEmail}</td><td>{order.quantity} × {order.sku}</td><td>{money.format(order.total)}</td><td><span className="policy">{order.fulfillmentPolicy}</span></td><td><span className={`status ${order.status.toLowerCase()}`}>{statusLabel[order.status]}</span></td></tr>)}
                {!orders.length && <tr><td colSpan={6} className="empty-table">No orders yet. Place the first order above.</td></tr>}
              </tbody>
            </table>
          </div>
        </section>

        <section className="panel inventory-panel" id="inventory">
          <div className="panel-heading"><div><p className="eyebrow">STOCK SNAPSHOT</p><h2>Inventory</h2></div><span className="architecture-chip">Optimistic locking</span></div>
          <div className="inventory-grid">
            {inventory.map((item) => {
              const total = item.available + item.reserved;
              const percent = total ? Math.round(item.available / total * 100) : 0;
              return <article className="inventory-card" key={item.sku}><div><strong>{item.name}</strong><small>{item.sku}</small></div><b>{item.available}</b><div className="stock-bar"><span style={{ width: `${percent}%` }} /></div><p>{item.reserved} reserved</p></article>;
            })}
          </div>
        </section>
      </main>
    </div>
  );
}

function Metric({ label, value, detail, tone = "default" }: { label: string; value: string; detail: string; tone?: string }) {
  return <article className={`metric ${tone}`}><p>{label}</p><strong>{value}</strong><small>{detail}</small></article>;
}
