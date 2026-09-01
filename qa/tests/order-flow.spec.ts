import { expect, test } from "@playwright/test";

test("places an order and displays its eventual status", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Order fulfillment, in real time." })).toBeVisible();

  const form = page.getByTestId("order-form");
  await form.getByLabel("Email").fill("qa@example.com");
  await form.getByLabel("Quantity").fill("1");
  await form.getByRole("button", { name: "Place order" }).click();

  const newestOrder = page.getByTestId("order-row").first();
  await expect(newestOrder).toContainText("qa@example.com");
  await expect(newestOrder).toContainText(/Confirmed|Rejected/, { timeout: 20_000 });
});

test("shows validation feedback for an invalid order", async ({ page }) => {
  await page.goto("/");
  const form = page.getByTestId("order-form");
  await form.getByLabel("Email").fill("not-an-email");
  await form.getByRole("button", { name: "Place order" }).click();
  await expect(form.getByLabel("Email")).toHaveJSProperty("validity.valid", false);
});

