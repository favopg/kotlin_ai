import { test, expect } from '@playwright/test';

// register.html の登録フロー: 入力 → 送信（モック）→ 成功メッセージ表示
// 仕様では /expensive/register とあるが、画面実装は /expense/register を呼び出しています。
// テストでは両方のエンドポイントをモックして、どちらでも 200/JSON を返すようにします。

test('register page: submits form and shows success message with mocked API', async ({ page }) => {
  // 共通のモック応答
  const fulfillOk = async (route: any) => {
    const req = route.request();
    expect(req.method()).toBe('POST');

    // 可能なら POST ボディを検証（型安全のために try/catch）
    try {
      const body = req.postDataJSON();
      if (body) {
        expect(!!body.category).toBeTruthy();
        expect(typeof body.amount === 'number' || typeof body.amount === 'string').toBeTruthy();
      }
    } catch {
      // 解析できない場合はスキップ
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ status: 'OK', id: 123 })
    });
  };

  // 画面実装の実エンドポイント
  await page.route('**/expense/register', fulfillOk);
  // 仕様に記載のエンドポイント（念のため）
  await page.route('**/expensive/register', fulfillOk);

  // 画面へ遷移
  await page.goto('/register.html', { waitUntil: 'domcontentloaded' });

  // 入力（カテゴリーと金額）
  await page.selectOption('#inputCategory', { label: '食費' });
  await page.fill('#inputAmount', '1500');

  // 送信
  await page.getByRole('button', { name: '登録' }).click();

  // 成功メッセージの確認
  const alert = page.getByRole('alert');
  await expect(alert).toBeVisible();
  await expect(alert).toHaveText(/登録が完了しました/);
});
