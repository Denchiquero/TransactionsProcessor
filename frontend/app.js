const products = [
    {
        id: 'prod_1',
        name: 'Масляный фильтр',
        category: 'engine',
        price: 1200,
        description: 'Высококачественный масляный фильтр для всех типов двигателей',
        image: 'images/321321321313131.jpg'
    },
    {
        id: 'prod_2',
        name: 'Тормозные колодки',
        category: 'brakes',
        price: 4500,
        description: 'Комплект тормозных колодок. Композитные тормозные колодки',
        image: 'images/kolodki.jpg'
    },
    {
        id: 'prod_3',
        name: 'Амортизатор',
        category: 'suspension',
        price: 3200,
        description: 'Газовый амортизатор передний левый/правый',
        image: 'images/amort.jpg'
    },
    {
        id: 'prod_4',
        name: 'Аккумулятор',
        category: 'electrics',
        price: 8900,
        description: 'Аккумулятор 63Ah, ток холодной прокрутки 610A',
        image: 'images/akkum.jpg'
    },
    {
        id: 'prod_5',
        name: 'Салонный фильтр',
        category: 'engine',
        price: 800,
        description: 'Воздушный фильтр салонный с угольным элементом',
        image: 'images/filter.jpg'
    },
    {
        id: 'prod_6',
        name: 'Свечи зажигания',
        category: 'engine',
        price: 2500,
        description: 'Иридиевые свечи зажигания, комплект 4 шт',
        image: 'images/svechi.jpg'
    },
    {
        id: 'prod_7',
        name: 'Тормозные диски',
        category: 'brakes',
        price: 5200,
        description: 'Комплект задних тормозных дисков',
        image: 'images/diski.jpg'
    },
    {
        id: 'prod_8',
        name: 'Стойка стабилизатора',
        category: 'suspension',
        price: 1500,
        description: 'Стойка стабилизатора поперечной устойчивости',
        image: ''
    },
    {
        id: 'prod_9',
        name: 'Генератор',
        category: 'electrics',
        price: 12500,
        description: 'Генератор 120A, восстановленный с гарантией',
        image: 'images/generator.jpg'
    },
    {
        id: 'prod_10',
        name: 'Ремень ГРМ',
        category: 'engine',
        price: 3800,
        description: 'Ремень газораспределительного механизма с роликами',
        image: 'images/GRM.jpg'
    },
    {
        id: 'prod_11',
        name: 'Тормозная жидкость',
        category: 'brakes',
        price: 600,
        description: 'Тормозная жидкость DOT-4, 1 литр',
        image: 'images/tormozuha.jpg'
    },
    {
        id: 'prod_12',
        name: 'Пружина подвески',
        category: 'suspension',
        price: 2800,
        description: 'Пружина передней подвески, пара',
        image: 'images/pruzhiny.jpg'
    },
    {
        id: 'prod_13',
        name: 'Стартер',
        category: 'electrics',
        price: 9800,
        description: 'Стартер восстановленный, гарантия 1 год',
        image: 'images/starter.jpg'
    },
    {
        id: 'prod_14',
        name: 'Топливный фильтр',
        category: 'engine',
        price: 1500,
        description: 'Топливный фильтр тонкой очистки',
        image: ''
    },
    {
        id: 'prod_15',
        name: 'Суппорт тормозной',
        category: 'brakes',
        price: 7500,
        description: 'Тормозной суппорт передний левый/правый',
        image: 'images/support.jpg'
    },
    {
        id: 'prod_16',
        name: 'Сайлентблок',
        category: 'suspension',
        price: 900,
        description: 'Сайлентблок рычага передней подвески',
        image: 'images/silentblock.jpg'
    },
    {
        id: 'prod_17',
        name: 'Фара передняя',
        category: 'electrics',
        price: 11200,
        description: 'Передняя фара левая/правая, с линзой',
        image: 'images/fara.jpg'
    },
    {
        id: 'prod_18',
        name: 'Прокладка ГБЦ',
        category: 'engine',
        price: 2200,
        description: 'Прокладка головки блока цилиндров',
        image: 'images/proklagka.jpg'
    },
    {
        id: 'prod_19',
        name: 'Тормозной шланг',
        category: 'brakes',
        price: 1200,
        description: 'Тормозной шланг передний/задний',
        image: 'images/shlang.jpg'
    },
    {
        id: 'prod_20',
        name: 'Подшипник ступицы',
        category: 'suspension',
        price: 3400,
        description: 'Подшипник передней ступицы',
        image: 'images/podshipnik.jpg'
    }
];

let cart = [];
let currentFilter = 'all';

function loadCartFromStorage() {
    const savedCart = localStorage.getItem('cart');
    if (savedCart) {
        cart = JSON.parse(savedCart);
        updateCartCount();
    }
}

// Инициализация
document.addEventListener('DOMContentLoaded', function() {
    renderProducts();
    updateCartCount();
    loadCartFromStorage();
});

// Рендер товаров
function renderProducts() {
    const grid = document.getElementById('products-grid');
    const filteredProducts = currentFilter === 'all'
        ? products
        : products.filter(p => p.category === currentFilter);

    grid.innerHTML = filteredProducts.map(product => `
        <div class="product-card">
            <div class="product-image">
                <img class='imge' src=${product.image} alt=${product.name}>
            </div>
            <div class="product-info">
                <div class="product-header">
                    <h2 class="product-name">${product.name}</h2>
                    <p class="product-category">${getCategoryName(product.category)}</p>
                </div>
                <p class="product-description">${product.description}</p>
                <div class="product-price">${product.price} руб</div>
                <button class="add-to-cart" id="add-to-cart" onclick="addToCart('${product.id}', this)">
                    Добавить в корзину
                </button>
            </div>
        </div>
    `).join('');
}

// Фильтрация товаров
function filterProducts(category) {
    currentFilter = category;
    document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
    renderProducts();
}

// Названия категорий
function getCategoryName(category) {
    const names = {
        'engine': 'Двигатель',
        'brakes': 'Тормоза',
        'suspension': 'Подвеска',
        'electrics': 'Электрика'
    };
    return names[category] || category;
}

function saveCartToStorage() {
    localStorage.setItem('cart', JSON.stringify(cart));
}

// Добавление в корзину
function addToCart(productId, button) {
    button.disabled = true;

    const product = products.find(p => p.id === productId);
    const existingItem = cart.find(item => item.id === productId);

    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({
            ...product,
            quantity: 1
        });
    }
    
    button.textContent = 'Товар добавлен в корзину!';
    setTimeout(() => {button.textContent = 'Добавить в корзину';
        button.disabled = false;}, 2000);
    updateCartCount();
    saveCartToStorage();
}

// Обновление счетчика корзины
function updateCartCount() {
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    document.getElementById('cart-count').textContent = totalItems;
}

// Открытие корзины
function openCart() {
    renderCartItems();
    document.getElementById('cart-modal').style.display = 'block';
    document.getElementById('body').style.overflow = 'hidden';
}

// Закрытие корзины
function closeCart() {
    document.getElementById('cart-modal').style.display = 'none';
    document.getElementById('body').style.overflow = 'scroll';
}

// Рендер товаров в корзине
function renderCartItems() {
    const container = document.getElementById('cart-items');
    const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

    if (cart.length === 0) {
        container.innerHTML = '<p class="empty-cart">Корзина пуста</p>';
    } else {
        container.innerHTML = cart.map(item => `
            <div class="cart-item">
                <div class="cart-item-info">
                    <h4>${item.name}</h4>
                    <div class="cart-item-price">${item.price} руб × ${item.quantity} = ${item.price * item.quantity} руб</div>
                </div>
                <div class="quantity-controls">
                    <button class="quantity-btn" onclick="changeQuantity('${item.id}', -1)">-</button>
                    <span>${item.quantity}</span>
                    <button class="quantity-btn" onclick="changeQuantity('${item.id}', 1)">+</button>
                    <button class="remove-btn" onclick="removeFromCart('${item.id}')">Удалить</button>
                </div>
            </div>
        `).join('');
    }

    document.getElementById('cart-total').textContent = total;
}

// Изменение количества
function changeQuantity(productId, change) {
    const item = cart.find(item => item.id === productId);
    if (item) {
        item.quantity += change;
        if (item.quantity <= 0) {
            cart = cart.filter(item => item.id !== productId);
        }
        updateCartCount();
        renderCartItems();
        saveCartToStorage();
    }
}

// Удаление из корзины
function removeFromCart(productId) {
    cart = cart.filter(item => item.id !== productId);
    updateCartCount();
    renderCartItems();
    saveCartToStorage();
}


//Форматирование номера карты
function formatCardNumber(input) {
    let value = input.value.replace(/\s/g, '').replace(/\D/g, '');
    let formattedValue = '';

    for (let i = 0; i < value.length; i++) {
        if (i > 0 && i % 4 === 0) {
            formattedValue += ' ';
        }
        formattedValue += value[i];
    }

    input.value = formattedValue.substring(0, 19);
}

//Получение токена карты
async function getCardToken(cardData) {
    try {
        const response = await fetch('/api/v1/tokens', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                cardNumber: cardData.number.replace(/\s/g, ''),
                expiryMonth: parseInt(cardData.expiryMonth),
                expiryYear: parseInt(cardData.expiryYear),
                cardholderName: cardData.name
            })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Ошибка получения токена');
        }

        const result = await response.json();
        return result.token;

    } catch (error) {
        console.error('Token error:', error);
        throw new Error(`Не удалось получить токен карты: ${error.message}`);
    }
}

//Валидация данных карты
function validateCardData(cardData) {
    const cardNumber = cardData.number.replace(/\s/g, '');
    if (!/^\d{16}$/.test(cardNumber)) {
        showAlertInCart('Номер карты должен содержать 16 цифр', 'error');
        return false;
    }

    const currentYear = new Date().getFullYear();
    const currentMonth = new Date().getMonth() + 1;
    const expiryYear = parseInt(cardData.expiryYear);
    const expiryMonth = parseInt(cardData.expiryMonth);

    if (expiryMonth < 0 || expiryMonth > 12){
        showAlertInCart('Неверный месяц истечения карты');
        return false;
    }

    if (expiryYear < currentYear ||
        (expiryYear === currentYear && expiryMonth < currentMonth)) {
        showAlertInCart('Срок действия карты истек', 'error');
        return false;
    }

    if (!/^\d{3,4}$/.test(cardData.cvv)) {
        showAlertInCart('CVV должен содержать 3 или 4 цифры', 'error');
        return false;
    }

    if (!cardData.name.trim()) {
        showAlertInCart('Введите имя владельца карты', 'error');
        return false;
    }

    return true;
}

//Оформление заказа с токенизацией
document.getElementById('checkout-form').addEventListener('submit', async function(e) {
    e.preventDefault();

    if (cart.length === 0) {
        showAlertInCart('Корзина пуста!', 'error');
        return;
    }

    const submitBtn = this.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = 'Обработка оплаты...';
    submitBtn.disabled = true;

    try {
        // 1. Получаем данные карты
        const cardData = {
            number: document.getElementById('card-number').value,
            expiryMonth: document.getElementById('card-expiry-month').value,
            expiryYear: document.getElementById('card-expiry-year').value,
            cvv: document.getElementById('card-cvv').value,
            name: document.getElementById('card-name').value
        };

        // 2. Валидируем карту
        if (!validateCardData(cardData)) {
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
            return;
        }

        // 3. Получаем токен от шлюза
        showAlertInCart('Получаем безопасный токен карты...', 'info');
        const cardToken = await getCardToken(cardData);

        // 4. Создаем заказ с токеном
        showAlertInCart('Создаем заказ...', 'info');
        const orderData = {
            customerName: document.getElementById('customer-name').value,
            customerEmail: document.getElementById('customer-email').value,
            customerPhone: document.getElementById('customer-phone').value,
            shippingAddress: document.getElementById('shipping-address').value,
            cardToken: cardToken, // ← передаем токен вместо данных карты
            cvv: document.getElementById('card-cvv').value,
            items: cart.map(item => ({
                productId: item.id,
                productName: item.name,
                quantity: item.quantity,
                price: item.price
            }))
        };

        const response = await fetch('/api/orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(orderData)
        });

        if (response.ok) {
            const order = await response.json();
            showAlertInCart(`Заказ успешно создан! Номер заказа: ${order.orderId}`, 'success');

            // Очищаем корзину
            cart = [];
            saveCartToStorage();
            updateCartCount();
            this.reset();

            // Закрываем корзину через 3 секунды
            setTimeout(() => {
                closeCart();
            }, 3000);
        } else {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Неизвестная ошибка при создании заказа');
        }

    } catch (error) {
        console.error('Checkout error:', error);
        showAlertInCart(`Ошибка: ${error.message}`, 'error');
    } finally {
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }
});

function showAlertInCart(message, type) {
    const container = document.getElementById('checkout-alert');
    container.innerHTML = `<div class="alert alert-${type}">${message}</div>`;
}

// Закрытие модального окна по клику вне его
window.onclick = function(event) {
    const modal = document.getElementById('cart-modal');
    if (event.target === modal) {
        closeCart();
    }
}