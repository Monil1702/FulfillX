import { describe, expect, it } from "vitest";
import { toNotification } from "../src/notification.js";

describe("toNotification", () => {
  it("converts a valid command", () => {
    const notification = toNotification({
      id: "event-1",
      orderId: "order-1",
      customerEmail: "buyer@example.com",
      outcome: "confirmed",
      message: "Order confirmed"
    });

    expect(notification.outcome).toBe("confirmed");
    expect(notification.receivedAt).toBeTruthy();
  });

  it("rejects an unsupported outcome", () => {
    expect(() => toNotification({
      id: "event-1",
      orderId: "order-1",
      customerEmail: "buyer@example.com",
      outcome: "unknown",
      message: "Bad command"
    })).toThrow(/outcome/);
  });
});

