const { chromium } = require('playwright');
const fs = require('fs');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  await page.goto('http://localhost:15500'); // or your actual URL

  // Step 1: Click the magnifier icon
  await page.locator('[aria-label="View output and test details"]').first().click({ force: true });

  // Step 2: Wait for modal content
  await page.waitForSelector('.MuiDialogContent-root');
 // Step 4: Get Prompt
   const prompt = await page.locator('div.css-jeo918').locator('p.css-an0y6n').nth(0).textContent();
   // Step 5: Get Output block
   const outputs = await page.locator('.MuiDialogContent-root').locator('div.MuiPaper-root').nth(1).textContent();
   // Step 6: Get Assertions Table Content
   const assertionRows = await page.locator('table.MuiTable-root tbody tr');
   const assertions = [];

  const count = await assertionRows.count();
    for (let i = 0; i < count; i++) {
      const row = assertionRows.nth(i);
      const cells = await row.locator('td').allTextContents();
      assertions.push(cells);
    }

    // Output to console or save
    console.log('Prompt:', prompt?.trim());
    console.log('Output:', outputs?.trim());
    console.log('Assertions:', assertions);

    // Clean output if needed
    const cleanedOutput = outputs.trim().replace(/\n{2,}/g, '\n\n');
    // Filter metadata if exists
    const filteredAssertions = assertions.filter(row => row[0] !== '_promptfooFileMetadata');

  // Step 7: Cretate reportData object and save to JSON
const reportData = {
  prompt: prompt.trim(),
  output: outputs.trim().replace(/\n{2,}/g, '\n\n'),
  assertions: filteredAssertions // Remove _promptfooFileMetadata if needed
};
fs.writeFileSync('frontend/static/reportData.json', JSON.stringify(reportData, null, 2));
  console.log("✅ Report saved to reportData.json");

  await browser.close();
})();
