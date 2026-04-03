const formatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });

// --- Dashboard ---
async function loadInvoices() {
    const body = document.getElementById('invoicesBody');
    if (!body) return;
    try {
        const res = await fetch('/api/admin/invoices');
        const invoices = await res.json();
        body.innerHTML = '';
        if(invoices.length === 0) {
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
    } catch(e) { console.error(e); }
}

// --- Products ---
let categoriesCache = [];

async function loadProducts() {
    const body = document.getElementById('productsBody');
    if (!body) return;
    try {
        // Pre-fetch categories for the dropdowns (cache busting to prevent stale browser data)
        const catRes = await fetch('/api/admin/tax-categories?t=' + new Date().getTime());
        categoriesCache = await catRes.json();

        const res = await fetch('/api/admin/products?t=' + new Date().getTime());
        const products = await res.json();
        body.innerHTML = '';
        products.forEach(p => {
            const tr = document.createElement('tr');
            
            let optionsHTML = '<option value="">No Category</option>';
            categoriesCache.forEach(c => {
                const selected = (p.taxCategoryId === c.id) ? 'selected' : '';
                optionsHTML += `<option value="${c.id}" ${selected}>${c.name} (${(c.taxRate*100).toFixed(1)}%)</option>`;
            });

            tr.innerHTML = `
                <td>${p.id}</td>
                <td><input type="text" class="inline-input" value="${p.name}" id="name-${p.id}"></td>
                <td><input type="number" class="inline-input" step="0.01" value="${p.price}" id="price-${p.id}"></td>
                <td><select class="inline-input" id="cat-${p.id}">${optionsHTML}</select></td>
                <td style="display: flex; gap: 0.5rem; justify-content: flex-end;">
                    <button class="btn-icon" style="color: #4ade80;" title="Save Changes" onclick="saveProduct(${p.id})"><i data-feather="save"></i></button>
                    <button class="btn-icon" title="Delete Product" onclick="deleteProduct(${p.id})"><i data-feather="trash"></i></button>
                </td>
            `;
            body.appendChild(tr);
        });
        if(window.feather) feather.replace();
    } catch(e) { console.error(e); }
}

async function saveProduct(id) {
    const name = document.getElementById(`name-${id}`).value;
    const price = document.getElementById(`price-${id}`).value;
    const catId = document.getElementById(`cat-${id}`).value;
    
    try {
        const res = await fetch(`/api/admin/products/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ name, price: parseFloat(price), taxCategoryId: catId ? parseInt(catId) : null })
        });
        if(res.ok) {
            alert('Product updated successfully!');
            loadProducts(); // Automatically refresh UI
        }
        else alert('Failed to update product.');
    } catch(e) { alert('Network Error'); }
}

async function deleteProduct(id) {
    if(!confirm("Are you sure you want to permanently delete this product?")) return;
    try {
        await fetch(`/api/admin/products/${id}`, { method: 'DELETE' });
        loadProducts();
    } catch(e) { alert('Failed'); }
}

async function openNewProduct() {
    const name = prompt("Enter new product name:");
    if(!name) return;
    const price = prompt("Enter product price USD:");
    if(!price) return;
    try {
        const res = await fetch('/api/admin/products', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ name, price: parseFloat(price) })
        });
        if(res.ok) loadProducts();
        else alert('Failed to create product.');
    } catch(e) { alert('Network Error'); }
}

// --- Categories (Settings) ---
async function loadSettings() {
    const body = document.getElementById('categoriesBody');
    if (!body) return;
    try {
        const res = await fetch('/api/admin/tax-categories?t=' + new Date().getTime());
        const cats = await res.json();
        body.innerHTML = '';
        cats.forEach(c => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${c.id}</td>
                <td><input type="text" class="inline-input" value="${c.name}" id="cname-${c.id}"></td>
                <td><input type="number" class="inline-input" step="0.01" value="${c.taxRate}" id="crate-${c.id}"></td>
                <td style="display: flex; gap: 0.5rem; justify-content: flex-end;">
                    <button class="btn-icon" style="color: #4ade80;" onclick="saveCategory(${c.id})"><i data-feather="save"></i></button>
                    <button class="btn-icon" onclick="deleteCategory(${c.id})"><i data-feather="trash"></i></button>
                </td>
            `;
            body.appendChild(tr);
        });
        if(window.feather) feather.replace();
    } catch(e) { console.error(e); }
}

async function saveCategory(id) {
    const name = document.getElementById(`cname-${id}`).value;
    const taxRate = document.getElementById(`crate-${id}`).value;
    try {
        await fetch(`/api/admin/tax-categories/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ name, taxRate: parseFloat(taxRate) })
        });
        alert('Category updated securely.');
        loadSettings(); // Force UI refresh
    } catch(e) {}
}

async function deleteCategory(id) {
    if(!confirm("Destroy category? Warning: Ensure no products are mapped to it or DB constraints will fail.")) return;
    try {
        await fetch(`/api/admin/tax-categories/${id}`, { method: 'DELETE' });
        loadSettings();
    } catch(e) {}
}

async function addNewCategory() {
    const name = prompt("Name (e.g. Services, Food):");
    if(!name) return;
    const rate = prompt("Tax Rate (e.g. 0.05 for 5%):");
    if(!rate) return;
    try {
        await fetch('/api/admin/tax-categories', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ name, taxRate: parseFloat(rate) })
        });
        loadSettings();
    } catch(e) {}
}
