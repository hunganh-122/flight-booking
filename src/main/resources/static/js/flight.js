document.addEventListener('DOMContentLoaded', () => {
    const prices = document.querySelectorAll('.price');
    prices.forEach(price => {
        price.addEventListener('click', () => {
            prices.forEach(p => p.classList.remove('selected'));
            price.classList.add('selected');
        });
    });
});