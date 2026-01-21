const revenueCtx = document.getElementById('revenueChart').getContext('2d');
const revenueChart = new Chart(revenueCtx, {
    type: 'line',
    data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
        datasets: [{
            label: 'Doanh thu',
            data: [12000000, 15000000, 13000000, 18000000, 19000000, 17000000, 22000000, 20000000, 25000000, 27000000, 30000000, 35000000],
            borderColor: 'rgba(0, 123, 255, 0.75)',
            backgroundColor: 'rgba(0, 123, 255, 0.3)',
            fill: true,
            tension: 0.4
        }]
    },
    options: {
        responsive: true,
        scales: {
            x: {
                beginAtZero: true
            },
            y: {
                beginAtZero: true
            }
        }
    }
});

const userCtx = document.getElementById('userChart').getContext('2d');
const userChart = new Chart(userCtx, {
    type: 'bar',
    data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
        datasets: [{
            label: 'Người dùng mới',
            data: [200, 300, 250, 400, 500, 350, 450, 400, 550, 600, 700, 800],
            borderColor: 'rgba(23, 162, 184, 0.75)',
            backgroundColor: 'rgba(23, 162, 184, 0.5)',
            fill: true
        }]
    },
    options: {
        responsive: true,
        scales: {
            x: {
                beginAtZero: true
            },
            y: {
                beginAtZero: true
            }
        }
    }
});