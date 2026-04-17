const formatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });

// --- Toast Notification ---
function showToast(msg, isError = false) {
    const toast = document.getElementById('toast');
    if (!toast) return;
    toast.textContent = msg;
    toast.style.background = isError ? '#ef4444' : '#22c55e';
    toast.style.display = 'block';
    setTimeout(() => { toast.style.display = 'none'; }, 3000);
}

// --- Inventory Module Toggle ---
let inventoryModuleEnabled = false;

async function loadInventoryToggle() {
    const toggle = document.getElementById('inventoryToggle');
    if (!toggle) return;
    try {
        const res = await fetch('/api/admin/settings/inventory-module');
        if (!res.ok) throw new Error('not ok');
        const data = await res.json();
        inventoryModuleEnabled = data.enabled;
        toggle.checked = data.enabled;
    } catch (e) {
        console.warn('Could not load inventory toggle state.');
    }
}

async function toggleInventoryModule(enabled) {
    inventoryModuleEnabled = enabled;
    try {
        await fetch('/api/admin/settings/inventory-module', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ enabled })
        });
        showToast(`Inventory Module ${enabled ? 'enabled' : 'disabled'}.`);
        if (typeof loadProducts === 'function') loadProducts();
    } catch (e) {
        showToast('Failed to save setting.', true);
    }
}

// --- Dashboard ---
async function loadInvoices() {
    const body = document.getElementById('invoicesBody');
    if (!body) return;
    try {
        const res = await fetch('/api/admin/invoices');
        if (!res.ok) throw new Error('Failed');
        const invoices = await res.json();
        body.innerHTML = '';
        if (invoices.length === 0) {
            body.innerHTML = '<tr><td colspan="4" style="text-align:center; padding: 2rem;">No invoices generated yet.</td></tr>';
            return;
        }
        invoices.reverse().forEach(inv => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#${inv.id}</td>
                <td>${new Date(inv.dateCreated).toLocaleDateString()}</td>
                <td>${inv.customer.name}</td>
                <td><strong style="color: var(--accent-color);">${formatter.format(inv.grandTotal)}</strong></td>
            `;
            body.appendChild(tr);
        });
    } catch (e) { console.error('loadInvoices error:', e); }
}

// --- Products ---
let categoriesCache = [];

async function loadProducts() {
    const body = document.getElementById('productsBody');
    const stockHeader = document.getElementById('stockHeader');
    if (!body) return;
    try {
        const catRes = await fetch('/api/admin/tax-categories?t=' + Date.now());
        if (!catRes.ok) throw new Error('Failed to load categories');
        categoriesCache = await catRes.json();

        const res = await fetch('/api/admin/products?t=' + Date.now());
        if (!res.ok) throw new Error('Failed to load products');
        const products = await res.json();

        // Populate Add Product modal category dropdown
        const newProdCat = document.getElementById('newProductCat');
        if (newProdCat) {
            newProdCat.innerHTML = '<option value="">No Category</option>';
            categoriesCache.forEach(c => {
                newProdCat.innerHTML += `<option value="${c.id}">${c.name} (${(c.taxRate * 100).toFixed(1)}%)</option>`;
            });
        }

        // Inventory stock fetch
        if (inventoryModuleEnabled) {
            if (stockHeader) stockHeader.style.display = '';
            await Promise.all(products.map(async p => {
                try {
                    const invRes = await fetch(`http://localhost:8082/api/inventory/${p.id}`);
                    p.stock = invRes.ok ? (await invRes.json()).stockQuantity : null;
                } catch (e) { p.stock = null; }
            }));
        } else {
            if (stockHeader) stockHeader.style.display = 'none';
        }

        body.innerHTML = '';
        products.forEach(p => {
            const tr = document.createElement('tr');

            let optionsHTML = '<option value="">No Category</option>';
            categoriesCache.forEach(c => {
                const selected = (p.taxCategoryId === c.id) ? 'selected' : '';
                optionsHTML += `<option value="${c.id}" ${selected}>${c.name} (${(c.taxRate * 100).toFixed(1)}%)</option>`;
            });

            const stockCell = inventoryModuleEnabled
                ? `<td><strong style="color: ${(p.stock > 0) ? '#4ade80' : '#f87171'}">${p.stock ?? 0}</strong></td>`
                : '';
            const adjustBtn = inventoryModuleEnabled
                ? `<button class="btn-icon" style="color: #60a5fa;" title="Adjust Stock" onclick="openStockModal(${p.id})"><i data-feather="package"></i></button>`
                : '';

            tr.innerHTML = `
                <td>${p.id}</td>
                <td><input type="text" class="inline-input" value="${p.name}" id="name-${p.id}"></td>
                <td><input type="number" class="inline-input" step="0.01" value="${p.price}" id="price-${p.id}"></td>
                <td><select class="inline-input" id="cat-${p.id}">${optionsHTML}</select></td>
                ${stockCell}
                <td style="display: flex; gap: 0.5rem; justify-content: flex-end;">
                    ${adjustBtn}
                    <button class="btn-icon" style="color: #4ade80;" title="Save Changes" onclick="saveProduct(${p.id})"><i data-feather="save"></i></button>
                    <button class="btn-icon" title="Delete Product" onclick="deleteProduct(${p.id})"><i data-feather="trash"></i></button>
                </td>
            `;
            body.appendChild(tr);
        });
        if (window.feather) feather.replace();
    } catch (e) {
        console.error('loadProducts error:', e);
        showToast('Failed to load products.', true);
    }
}

async function saveProduct(id) {
    const name = document.getElementById(`name-${id}`).value.trim();
    const price = document.getElementById(`price-${id}`).value;
    const catId = document.getElementById(`cat-${id}`).value;

    if (!name || !price) { showToast('Name and price are required.', true); return; }

    try {
        const res = await fetch(`/api/admin/products/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, price: parseFloat(price), taxCategoryId: catId ? parseInt(catId) : null })
        });
        if (res.ok) { showToast('Product updated!'); loadProducts(); }
        else { showToast('Failed to update product.', true); }
    } catch (e) { showToast('Network error.', true); }
}

async function deleteProduct(id) {
    if (!confirm('Are you sure you want to permanently delete this product?')) return;
    try {
        const res = await fetch(`/api/admin/products/${id}`, { method: 'DELETE' });
        if (res.ok) { showToast('Product deleted.'); loadProducts(); }
        else { showToast('Failed to delete product.', true); }
    } catch (e) { showToast('Network error.', true); }
}

// --- Add Product Modal ---
function openNewProduct() {
    document.getElementById('newProductName').value = '';
    document.getElementById('newProductPrice').value = '';
    document.getElementById('newProductCat').value = '';
    document.getElementById('productModal').classList.add('open');
    setTimeout(() => document.getElementById('newProductName').focus(), 50);
}

function closeProductModal() {
    document.getElementById('productModal').classList.remove('open');
}

async function submitNewProduct() {
    const name = document.getElementById('newProductName').value.trim();
    const price = document.getElementById('newProductPrice').value;
    const catId = document.getElementById('newProductCat').value;

    if (!name) { showToast('Product name is required.', true); return; }
    if (!price || parseFloat(price) <= 0) { showToast('A valid price is required.', true); return; }

    try {
        const res = await fetch('/api/admin/products', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, price: parseFloat(price), taxCategoryId: catId ? parseInt(catId) : null })
        });
        if (res.ok) {
            closeProductModal();
            showToast('Product created!');
            loadProducts();
        } else {
            const err = await res.text();
            showToast('Failed to create product: ' + err, true);
        }
    } catch (e) { showToast('Network error.', true); }
}

// --- Adjust Stock Modal ---
function openStockModal(productId) {
    document.getElementById('stockProductId').value = productId;
    document.getElementById('stockChange').value = '';
    document.getElementById('stockReason').value = '';
    document.getElementById('stockModal').classList.add('open');
    setTimeout(() => document.getElementById('stockChange').focus(), 50);
}

function closeStockModal() {
    document.getElementById('stockModal').classList.remove('open');
}

async function submitStockAdjust() {
    const productId = parseInt(document.getElementById('stockProductId').value);
    const change = parseInt(document.getElementById('stockChange').value);
    const reason = document.getElementById('stockReason').value.trim() || 'Manual Adjustment';

    if (isNaN(change)) { showToast('Please enter a valid number.', true); return; }

    try {
        const res = await fetch('http://localhost:8082/api/inventory/adjust', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId, quantityChange: change, reason })
        });
        if (res.ok) {
            closeStockModal();
            showToast('Stock adjusted!');
            loadProducts();
        } else { showToast('Failed to adjust stock. Is InventoryServer running?', true); }
    } catch (e) { showToast('Cannot reach InventoryServer.', true); }
}

// --- Tax Categories ---
async function loadSettings() {
    const body = document.getElementById('categoriesBody');
    if (!body) return;
    try {
        const res = await fetch('/api/admin/tax-categories?t=' + Date.now());
        if (!res.ok) throw new Error('Failed');
        const cats = await res.json();
        body.innerHTML = '';
        if (cats.length === 0) {
            body.innerHTML = '<tr><td colspan="4" style="text-align:center; padding: 2rem; color: var(--text-secondary);">No categories yet. Add one above.</td></tr>';
            return;
        }
        cats.forEach(c => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${c.id}</td>
                <td><input type="text" class="inline-input" value="${c.name}" id="cname-${c.id}"></td>
                <td><input type="number" class="inline-input" step="0.01" value="${c.taxRate}" id="crate-${c.id}"></td>
                <td style="display: flex; gap: 0.5rem; justify-content: flex-end;">
                    <button class="btn-icon" style="color: #4ade80;" onclick="saveCategory(${c.id})" title="Save"><i data-feather="save"></i></button>
                    <button class="btn-icon" onclick="deleteCategory(${c.id})" title="Delete"><i data-feather="trash"></i></button>
                </td>
            `;
            body.appendChild(tr);
        });
        if (window.feather) feather.replace();
    } catch (e) {
        console.error('loadSettings error:', e);
        showToast('Failed to load categories.', true);
    }
}

async function saveCategory(id) {
    const name = document.getElementById(`cname-${id}`).value.trim();
    const taxRate = document.getElementById(`crate-${id}`).value;
    if (!name) { showToast('Category name is required.', true); return; }
    try {
        const res = await fetch(`/api/admin/tax-categories/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, taxRate: parseFloat(taxRate) })
        });
        if (res.ok) { showToast('Category updated!'); loadSettings(); }
        else { showToast('Failed to update category.', true); }
    } catch (e) { showToast('Network error.', true); }
}

async function deleteCategory(id) {
    if (!confirm('Delete this category? Products using it will lose their tax assignment.')) return;
    try {
        const res = await fetch(`/api/admin/tax-categories/${id}`, { method: 'DELETE' });
        if (res.ok) { showToast('Category deleted.'); loadSettings(); }
        else { showToast('Failed to delete category.', true); }
    } catch (e) { showToast('Network error.', true); }
}

// --- Add Category Modal ---
function openCategoryModal() {
    document.getElementById('newCatName').value = '';
    document.getElementById('newCatRate').value = '';
    document.getElementById('categoryModal').classList.add('open');
    setTimeout(() => document.getElementById('newCatName').focus(), 50);
}

function closeCategoryModal() {
    document.getElementById('categoryModal').classList.remove('open');
}

async function submitNewCategory() {
    const name = document.getElementById('newCatName').value.trim();
    const rate = document.getElementById('newCatRate').value;

    if (!name) { showToast('Category name is required.', true); return; }
    if (rate === '' || isNaN(parseFloat(rate))) { showToast('A valid tax rate is required.', true); return; }

    try {
        const res = await fetch('/api/admin/tax-categories', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, taxRate: parseFloat(rate) })
        });
        if (res.ok) {
            closeCategoryModal();
            showToast('Category created!');
            loadSettings();
        } else {
            const err = await res.text();
            showToast('Failed: ' + err, true);
        }
    } catch (e) { showToast('Network error.', true); }
}

// Legacy compatibility aliases
function addNewCategory() { openCategoryModal(); }
function openNewProduct() {
    // Ensure categories are loaded before opening modal
    if (typeof loadProducts === 'function' && categoriesCache.length === 0) {
        fetch('/api/admin/tax-categories').then(r => r.json()).then(cats => {
            categoriesCache = cats;
            const sel = document.getElementById('newProductCat');
            if (sel) {
                sel.innerHTML = '<option value="">No Category</option>';
                cats.forEach(c => sel.innerHTML += `<option value="${c.id}">${c.name}</option>`);
            }
        }).catch(() => {});
    }
    document.getElementById('newProductName').value = '';
    document.getElementById('newProductPrice').value = '';
    document.getElementById('productModal').classList.add('open');
    setTimeout(() => document.getElementById('newProductName').focus(), 50);
}

// Close modals on overlay click
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('open');
    }
});
