# Enterprise Billing System - User Guide

Welcome to the Enterprise Billing software! This platform gives you absolute control over your core retail operations, from executing Point-Of-Sale (POS) checkouts to governing global taxation economics for your products.

## Table of Contents
1. [The Point-Of-Sale (POS) Register](#1-the-point-of-sale-pos-register)
2. [Generating Invoices](#2-generating-invoices)
3. [The Admin Dashboard](#3-the-admin-dashboard)
4. [Category-Driven Taxation](#4-category-driven-taxation)
5. [Product Management](#5-product-management)

---

### 1. The Point-Of-Sale (POS) Register
**URL:** `http://localhost:8080/new-bill`

Designed for extreme speed at the checkout counter, the POS Register allows cashiers to quickly search for products by name and add them to a customer's cart. 
- You can dynamically adjust quantities on the fly.
- Global math—including the underlying Subtotal, unique Line-Item Taxes, and Grand Totals—are instantly computed visibly before the cashier finalizes the purchase.

### 2. Generating Invoices
**URL:** `http://localhost:8080/invoices/{id}/print`

Once a checkout registers successfully, you are forwarded to the **Print View**. This generates an A4 paper-optimized PDF document outlining:
- Billed Customer Identity & Number
- Receipt ID
- Itemized Purchase Grids
- Distinct Financial breakdowns

> **Tip:** You can use your browser shortcut (Ctrl+P / Cmd+P) to print straight to a receipt printer, or use the "Save HTML as PDF" button. 

### 3. The Admin Dashboard
**URL:** `http://localhost:8080/admin`

The Admin Dashboard provides upper-level managers a secure "Glassmorphism" interface to audit store behaviors over time.
- **Invoice Auditing**: The Dashboard home page shows a historical, uneditable digest trace of all past purchases submitted by users. You can immediately see the Dates, Customers, and financial revenue flowing through your system.

### 4. Category-Driven Taxation
**URL:** `http://localhost:8080/admin/settings`

Taxes are not created equally! To meet Enterprise Resource Planning (ERP) standards, the system allows you to build custom **Tax Categories**.
- A **Tax Category** represents a rule (e.g., *Electronics = 15%, Groceries = 0%, Accessories = 5%*). 
- Using the Settings panel, you can Add, Edit, or Remove these base categories. If tax-laws shift in your area, updating the number here trickles down automatically.

### 5. Product Management
**URL:** `http://localhost:8080/admin/products`

The heart of your inventory catalog. Visually interact with a live data-grid to manage your stock!
- **Data Editing**: Quickly overwrite a product's text Name, or price it dynamically.
- **Tax Mapping**: A drop-down menu exists on every given product, allowing you to "assign" it directly to a Tax Category bracket. 
- **Destructive Deletion**: Click the trash icon to permanently destroy the stock.

*(System version 1.0)*
