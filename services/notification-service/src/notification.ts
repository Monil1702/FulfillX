export type Notification = {
  id: string;
  orderId: string;
  customerEmail: string;
  outcome: "confirmed" | "rejected";
  message: string;
  receivedAt: string;
};

export function toNotification(input: unknown): Notification {
  if (!input || typeof input !== "object") {
    throw new Error("Notification command must be an object");
  }
  const value = input as Record<string, unknown>;
  for (const field of ["id", "orderId", "customerEmail", "outcome", "message"]) {
    if (typeof value[field] !== "string" || value[field] === "") {
      throw new Error(`Notification command has invalid ${field}`);
    }
  }
  if (value.outcome !== "confirmed" && value.outcome !== "rejected") {
    throw new Error("Notification outcome must be confirmed or rejected");
  }
  return {
    id: value.id as string,
    orderId: value.orderId as string,
    customerEmail: value.customerEmail as string,
    outcome: value.outcome,
    message: value.message as string,
    receivedAt: new Date().toISOString()
  };
}

