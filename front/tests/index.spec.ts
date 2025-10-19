import { test, expect } from '@playwright/test';

// Assumes the backend/server is running and serving the front directory at http://localhost:8080
// Test requirement: Access http://localhost:8080/index.html and verify the page is displayed

test('index page should be accessible and visible', async ({ page }) => {
  // Navigate to the index page using the configured baseURL
  await page.goto('/index.html', { waitUntil: 'domcontentloaded' });

  // Verify the page title from front/index.html
  await expect(page).toHaveTitle(/Kotlin APIサンプル/);

  // Check that some key elements are visible to ensure the page rendered correctly
  await expect(page.getByRole('heading', { level: 5, name: 'メニュー' })).toBeVisible();
  await expect(page.locator('main .card-header')).toHaveText('コンテンツ');

  // Optional: verify that the table exists
  await expect(page.locator('table')).toBeVisible();
});
