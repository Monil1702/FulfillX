import { Builder, By, until } from "selenium-webdriver";
import chrome from "selenium-webdriver/chrome.js";

const options = new chrome.Options();
if (process.env.HEADLESS !== "false") options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");

const driver = await new Builder().forBrowser("chrome").setChromeOptions(options).build();
try {
  await driver.get(process.env.BASE_URL ?? "http://localhost:3000");
  const heading = await driver.wait(until.elementLocated(By.css("h1")), 10_000);
  const text = await heading.getText();
  if (text !== "Order fulfillment, in real time.") {
    throw new Error(`Unexpected heading: ${text}`);
  }
  console.log("Selenium smoke test passed");
} finally {
  await driver.quit();
}

