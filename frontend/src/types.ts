export type OrderStatus = "PENDING_INVENTORY" | "PRIORITY_PENDING" | "CONFIRMED" | "REJECTED";

export type Order = {
  id: string;
  customerEmail: string;
  sku: string;
  quantity: number;
  unitPrice: number;
  total: number;
  status: OrderStatus;
  fulfillmentPolicy: "STANDARD" | "PRIORITY";
  createdAt: string;
  updatedAt: string;
};

export type InventoryItem = {
  sku: string;
  name: string;
  available: number;
  reserved: number;
};

export type Notification = {
  id: string;
  orderId: string;
  customerEmail: string;
  outcome: "confirmed" | "rejected";
  message: string;
  receivedAt: string;
};

export type CreateOrder = {
  customerEmail: string;
  sku: string;
  quantity: number;
  unitPrice: number;
  priority: boolean;
};

