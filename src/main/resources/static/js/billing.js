document.addEventListener('DOMContentLoaded', () => {
    // Global State
    const state = {
        customerId: null,
        items: [], // { uuid, productId, productName, price, quantity, lineTotal }
    };

    let searchTimeout = null;

    // DOM Attachments
    const custInput = document.getElementById('customerSearchInput');
    const custResults = document.getElementById('customerResults');
    const selCustInfo = document.getElementById('selectedCustomerInfo');
    const custSearchGroup = document.getElementById('customer-search-group');
    const selCustName = document.getElementById('selCustName');
    const selCustPhone = document.getElementById('selCustPhone');
    const clearCustBtn = document.getElementById('clearCustomerBtn');
    
    const uiSubtotal = document.getElementById('uiSubtotal');
    const uiTax = document.getElementById('uiTax');
    const uiGrandTotal = document.getElementById('uiGrandTotal');

    const invoiceItemsBody = document.getElementById('invoiceItemsBody');
    const addLineBtn = document.getElementById('addLineBtn');
    const saveInvoiceBtn = document.getElementById('saveInvoiceBtn');
    const saveSuccessMsg = document.getElementById('saveSuccessMsg');

    // Formatter
    const formatter = new Intl.NumberFormat('en-US', {
        style: 'currency',currency: 'USD',
    });

    // --- 1. Customer Binding ---
    custInput.addEventListener('input', (e) => {
        const query = e.target.value.trim();
        clearTimeout(searchTimeout);
        if (query.length < 2) {
            custResults.classList.remove('active');
            return;
        }

        searchTimeout = setTimeout(async () => {
            try {
                const res = await fetch(`/api/customers/search?q=${encodeURIComponent(query)}`);
                const customers = await res.json();
                renderCustomerResults(customers);
            } catch (err) { }
        }, 200);
    });

    function renderCustomerResults(customers) {
        custResults.innerHTML = '';
        if (customers.length === 0) {
            custResults.innerHTML = '<div class="autocomplete-item"><span style="color: var(--text-secondary)">No results found.</span></div>';
        } else {
            customers.forEach(cust => {
                const div = document.createElement('div');
                div.className = 'autocomplete-item';
                div.innerHTML = `<strong>${cust.name}</strong> <span style="float: right; color: var(--text-secondary)">${cust.phone}</span>`;
                div.addEventListener('click', () => {
                    state.customerId = cust.id;
                    selCustName.textContent = cust.name;
                    selCustPhone.textContent = cust.phone;
                    custSearchGroup.style.display = 'none';
                    selCustInfo.style.display = 'flex';
                    custResults.classList.remove('active');
                    custInput.value = '';
                });
                custResults.appendChild(div);
            });
        }
        custResults.classList.add('active');
    }

    clearCustBtn.addEventListener('click', () => {
        state.customerId = null;
        custSearchGroup.style.display = 'flex';
        selCustInfo.style.display = 'none';
        custInput.focus();
    });

    // --- 2. Invoice Items Logic ---
    function generateUUID() {
        return Math.random().toString(36).substring(2) + Date.now().toString(36);
    }

    addLineBtn.addEventListener('click', () => {
        state.items.push({
            uuid: generateUUID(),
            productId: null, productName: '', price: 0, quantity: 1, lineTotal: 0
        });
        renderItems();
    });

    function renderItems() {
        invoiceItemsBody.innerHTML = '';
        if (state.items.length === 0) {
            invoiceItemsBody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-secondary); padding: 2rem;">No items array mapped. Click '+ Add Line'.</td></tr>`;
            return;
        }

        state.items.forEach((item) => {
            const tr = document.createElement('tr');
            
            // Product Dropdown
            const tdProduct = document.createElement('td');
            const wrapper = document.createElement('div');
            wrapper.style.position = 'relative';
            const prodInput = document.createElement('input');
            prodInput.type = 'text';
            prodInput.placeholder = 'Search product...';
            prodInput.value = item.productName || '';
            prodInput.style.width = '100%';

            const prodResults = document.createElement('div');
            prodResults.className = 'autocomplete-results';
            
            let pSearchTimeout = null;
            prodInput.addEventListener('input', (e) => {
                const query = e.target.value.trim();
                clearTimeout(pSearchTimeout);
                if (query.length < 2) { prodResults.classList.remove('active'); return; }
                pSearchTimeout = setTimeout(async () => {
                    const res = await fetch(`/api/products/search?q=${encodeURIComponent(query)}`);
                    const products = await res.json();
                    prodResults.innerHTML = '';
                    products.forEach(p => {
                        const pdiv = document.createElement('div');
                        pdiv.className = 'autocomplete-item';
                        pdiv.innerHTML = `<strong>${p.name}</strong> <span style="float: right;">${formatter.format(p.price)}</span>`;
                        pdiv.addEventListener('click', () => {
                            item.productId = p.id; item.productName = p.name;
                            item.price = p.price; item.lineTotal = item.price * item.quantity;
                            prodResults.classList.remove('active');
                            calculateTotals(); renderItems();
                        });
                        prodResults.appendChild(pdiv);
                    });
                    if (products.length === 0) prodResults.innerHTML = '<div class="autocomplete-item">No results</div>';
                    prodResults.classList.add('active');
                }, 200);
            });

            document.addEventListener('click', (e) => {
                if (!prodInput.contains(e.target) && !prodResults.contains(e.target)) prodResults.classList.remove('active');
            });

            wrapper.appendChild(prodInput); wrapper.appendChild(prodResults);
            tdProduct.appendChild(wrapper);

            // Unit Price
            const tdPrice = document.createElement('td');
            tdPrice.textContent = formatter.format(item.price || 0);

            // Quantity
            const tdQty = document.createElement('td');
            const qtyInput = document.createElement('input');
            qtyInput.type = 'number'; qtyInput.min = '1';
            qtyInput.value = item.quantity;
            qtyInput.style.width = '100%'; qtyInput.style.padding = '0.5rem';
            qtyInput.addEventListener('input', (e) => {
                let val = parseInt(e.target.value);
                if (isNaN(val) || val < 1) val = 1;
                item.quantity = val;
                item.lineTotal = item.price * item.quantity;
                calculateTotals();
                tr.querySelector('.td-lt').textContent = formatter.format(item.lineTotal);
            });
            tdQty.appendChild(qtyInput);

            // Line Total
            const tdLineTotal = document.createElement('td');
            tdLineTotal.className = 'text-right td-lt';
            tdLineTotal.textContent = formatter.format(item.lineTotal || 0);

            // Delete action
            const tdAction = document.createElement('td');
            tdAction.style.textAlign = 'right';
            const removeBtn = document.createElement('button');
            removeBtn.className = 'btn-icon';
            removeBtn.innerHTML = '<i data-feather="trash-2"></i>';
            removeBtn.addEventListener('click', () => {
                state.items = state.items.filter(i => i.uuid !== item.uuid);
                renderItems(); calculateTotals();
            });
            tdAction.appendChild(removeBtn);

            tr.appendChild(tdProduct); tr.appendChild(tdPrice);
            tr.appendChild(tdQty); tr.appendChild(tdLineTotal); tr.appendChild(tdAction);
            invoiceItemsBody.appendChild(tr);
        });
        
        if(window.feather) feather.replace();
    }

    // --- 3. Live Totals Math ---
    function calculateTotals() {
        let subtotal = 0;
        state.items.forEach(i => subtotal += (i.lineTotal || 0));
        const tax = subtotal * 0.10;
        const grandTotal = subtotal + tax;

        uiSubtotal.textContent = formatter.format(subtotal);
        uiTax.textContent = formatter.format(tax);
        uiGrandTotal.textContent = formatter.format(grandTotal);
    }

    // --- 4. Remote Persistence ---
    saveInvoiceBtn.addEventListener('click', async () => {
        if (!state.customerId) { alert('Please select a customer first.'); return; }
        
        const validItems = state.items.filter(i => i.productId != null && i.quantity > 0);
        if (validItems.length === 0) { alert('Please add at least one valid product line.'); return; }

        saveInvoiceBtn.disabled = true;
        saveInvoiceBtn.textContent = 'Authenticating...';

        try {
            const response = await fetch('/api/invoices', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    customerId: state.customerId,
                    items: validItems.map(i => ({ productId: i.productId, quantity: i.quantity }))
                })
            });

            if (!response.ok) throw new Error('Network response failed');
            
            const responseData = await response.json();
            
            saveInvoiceBtn.style.display = 'none';
            saveSuccessMsg.style.display = 'block';
            
            // Redirect to Print Screen
            setTimeout(() => {
                window.location.href = `/invoices/${responseData.id}/print`;
            }, 800);

        } catch (err) {
            alert('Failed to save to database backend.');
            saveInvoiceBtn.disabled = false;
            saveInvoiceBtn.textContent = 'Save Invoice';
        }
    });

    // Start with 1 empty line
    addLineBtn.click();
});
