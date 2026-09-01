import type { CreateOrder, InventoryItem, Notification, Order } from "./types";

export const urls = {
  orders: import.meta.env.VITE_ORDER_API_URL ?? "http://localhost:8080",
  inventory: import.meta.env.VITE_INVENTORY_API_URL ?? "http://localhost:8081",
  notifications: import.meta.env.VITE_NOTIFICATION_API_URL ?? "http://localhost:3001"
};

async function read<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const problem = await response.json().catch(() => ({ detail: "Request failed" }));
    throw new Error(problem.detail ?? `Request failed with ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export const api = {
  listOrders: () => fetch(`${urls.orders}/api/orders`).then(read<Order[]>),
  createOrder: (order: CreateOrder) => fetch(`${urls.orders}/api/orders`, {
    method: "POST",
    headers: { "Content-Type": "application/json", "X-Correlation-ID": crypto.randomUUID() },
    body: JSON.stringify(order)
  }).then(read<Order>),
  listInventory: () => fetch(`${urls.inventory}/api/inventory`).then(read<InventoryItem[]>),
  listNotifications: () => fetch(`${urls.notifications}/api/notifications`).then(read<Notification[]>)
};

