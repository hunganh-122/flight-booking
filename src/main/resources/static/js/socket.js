const socket = new WebSocket('ws://localhost:8080/ws/seats');

socket.onopen = () => {
    console.log('Connected to WebSocket server');
};

socket.onmessage = (event) => {
    console.log("Received message from server:", event.data);
    try {
        const data = JSON.parse(event.data);
        console.log('Parsed data:', data);

        if (data.action === 'lockSeats') {
            console.log('Processing updateSeats message', data);
            updateSeatStatus(data.seatIds);
        } else {
            console.warn('Unexpected action:', data.action);
        }
    } catch (e) {
        console.error('Error parsing WebSocket message:', e);
    }
};

socket.onerror = (error) => {
    console.error('WebSocket error:', error);
};

socket.onclose = () => {
    console.log('WebSocket connection closed');
};


function updateSeatStatus(seats) {
    for (i = 0; i <= seats.length; i++) {
        const seatElement = document.getElementById(seats[i]);
        console.log("bảo kỳ"+seatElement)
        if (seatElement) {
                console.log("log")
                seatElement.disabled = true;
                seatElement.classList.remove('available');
                seatElement.classList.add('unavailable');

        } else {
            console.warn('Seat not found:', seat.id);
        }
    }
}


document.querySelector('form').addEventListener('submit', (event) => {
    event.preventDefault();

    const selectedSeats = [];
    document.querySelectorAll('.seat input:checked').forEach(seat => {
        selectedSeats.push(seat.getAttribute('id'));
    });

    if (selectedSeats.length > 0) {

        socket.send(JSON.stringify({ action: 'lockSeats', seatIds: selectedSeats }));
        setTimeout(() => {
            event.target.submit();
        }, 1000);
    } else {
        console.warn('No seats selected');
    }
});
