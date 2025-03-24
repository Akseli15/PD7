const API_URL = 'https://bot-techsupport.rightscan.ru:8078/api';
let chatId = new URLSearchParams(window.location.search).get('chatId');
let orderId = null;

document.addEventListener('DOMContentLoaded', () => {
    loadCategories();
    loadMenuItems();
    loadCartItems();
    document.getElementById('finalize-order').addEventListener('click', finalizeOrder);
});

async function loadCategories() {
    try {
        const response = await fetch(`${API_URL}/category`);
        const categories = await response.json();
        displayCategories(categories);
    } catch (error) {
        console.error('Ошибка загрузки категорий:', error);
    }
}

function displayCategories(categories) {
    const container = document.getElementById('categories-container');
    container.innerHTML = '';
    categories.forEach(category => {
        const button = document.createElement('button');
        button.textContent = category.name;
        button.classList.add('btn', 'btn-primary', 'm-1');
        button.onclick = () => loadMenuItems(category.id);
        container.appendChild(button);
    });
}

async function loadMenuItems(categoryId = null) {
    let url = `${API_URL}/menu/available`;
    if (categoryId) url = `${API_URL}/menu/by-category?categoryId=${categoryId}`;

    try {
        const response = await fetch(url);
        const items = await response.json();
        displayMenuItems(items);
    } catch (error) {
        console.error('Ошибка загрузки меню:', error);
    }
}

function displayMenuItems(items) {
    const container = document.getElementById('menu-container');
    container.innerHTML = '';

    items.forEach(item => {
        const menuItem = document.createElement('div');
        menuItem.classList.add('col-md-5', 'col-6', 'mb-2');
        menuItem.innerHTML = `
            <div class="card">
                <img src="${item.imageUrl}" class="card-img-top" alt="${item.name}">
                <div class="card-body">
                    <h5 class="card-title">${item.name}</h5>
                    <p class="card-text">${item.price} ₽</p>
                    <button class="btn btn-success w-100" onclick="addToCart(${item.id})">Добавить</button>
                </div>
            </div>
        `;
        container.appendChild(menuItem);
    });
}

async function addToCart(menuItemId) {
    if (!orderId) await createOrder();

    try {
        await fetch(`${API_URL}/order/${orderId}/addItem?menuItemId=${menuItemId}&quantity=1`, { method: 'POST' });
        loadCartItems();
    } catch (error) {
        console.error('Ошибка добавления в корзину:', error);
    }
}

async function createOrder() {
    try {
        const response = await fetch(`${API_URL}/order/create?chatId=${chatId}`, { method: 'POST' });
        const order = await response.json();
        orderId = order.id;
    } catch (error) {
        console.error('Ошибка создания заказа:', error);
    }
}

async function loadCartItems() {
    if (!orderId) return;

    try {
        const response = await fetch(`${API_URL}/${orderId}/items`);
        const items = await response.json();
        displayCartItems(items);
    } catch (error) {
        console.error('Ошибка загрузки корзины:', error);
    }
}

function displayCartItems(items) {
    const container = document.getElementById('cart-container');
    container.innerHTML = '';

    items.forEach(item => {
        const cartItem = document.createElement('div');
        cartItem.classList.add('col-12', 'mb-2');
        cartItem.innerHTML = `
            <div class="d-flex justify-content-between align-items-center border p-2">
                <span>${item.menuItem.name} (${item.quantity} шт.)</span>
                <button class="btn btn-danger btn-sm" onclick="removeFromCart(${item.id})">Удалить</button>
            </div>
        `;
        container.appendChild(cartItem);
    });
}

async function removeFromCart(itemId) {
    try {
        await fetch(`${API_URL}/order/${orderId}/removeItem/${itemId}`, { method: 'DELETE' });
        loadCartItems();
    } catch (error) {
        console.error('Ошибка удаления из корзины:', error);
    }
}

async function finalizeOrder() {
    if (!orderId) return;

    try {
        await fetch(`${API_URL}/${orderId}/finalize`, { method: 'POST' });
        alert('Заказ оформлен!');
        orderId = null;
        loadCartItems();
    } catch (error) {
        console.error('Ошибка оформления заказа:', error);
    }
}