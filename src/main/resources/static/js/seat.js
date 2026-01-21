function updateSummary() {
    // find selected seat containers (div.seat.selected)
    const selected = document.querySelectorAll('.seat.selected');
    const selectedCountValue = selected.length;

    let selectedSeats = [];
    let totalPrice = 0;

    selected.forEach(seatDiv => {
        const input = seatDiv.querySelector('input[type="checkbox"]');
        if (!input) return;
        const seatNumber = input.dataset.seatNumber || '';
        const seatId = input.dataset.seatId || '';
        const price = parseFloat(input.dataset.seatPrice) || 0;
        selectedSeats.push(seatNumber);
        totalPrice += price;
    });

    document.getElementById('selected-count').textContent = selectedCountValue;
    document.getElementById('selected-seats').textContent = selectedSeats.join(', ');
    document.getElementById('total-price').textContent = totalPrice.toLocaleString('vi-VN') + ' VND';

    const seatIds = Array.from(selected).map(seatDiv => {
        const input = seatDiv.querySelector('input[type="checkbox"]');
        return input ? input.dataset.seatId : '';
    }).filter(Boolean);
    document.getElementById('seat-ids').value = seatIds.join(',');
}

function updateSeatInputValue() {

    let selectedSeatsText = document.getElementById('selected-seats').innerText;
    let seatInput = document.getElementById('seat-input');
    seatInput.value = selectedSeatsText;
}

function toggleSeat(seatElement) {
    const selected = document.querySelectorAll('.seat .selected');
    if (selected.length > 9 && seatElement.checked) {
        Swal.fire({
            icon: "error",
            title: "Tối đa chọn 10 ghế!",
        });
        seatElement.checked = false;
        return;
    }
    if (seatElement.classList.contains('unavailable') || seatElement.classList.contains('hold')) return;

    seatElement.classList.toggle('selected');
    updateSummary();
    updateSeatInputValue();
}

const payForm = document.querySelector('form[action="/pay"]');
if (payForm) {
    payForm.addEventListener('submit', function() {
        updateSeatInputValue();
    });
}



