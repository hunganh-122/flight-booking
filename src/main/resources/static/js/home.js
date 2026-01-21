new WOW().init();

let departurePicker;
let returnPicker;

flatpickr(".date-flatpickr", {
    dateFormat: 'd-m-Y',
    minDate: 'today',
    maxDate: new Date().fp_incr(180)
});

$(document).ready(function () {
    // initialize individual pickers if present
    if (document.getElementById('departure-date')) {
        departurePicker = flatpickr('#departure-date', {
            dateFormat: 'd-m-Y',
            minDate: 'today',
            onChange: function(selectedDates) {
                if (returnPicker && selectedDates && selectedDates[0]) {
                    returnPicker.set('minDate', selectedDates[0]);
                }
            }
        });
    }
    if (document.getElementById('return-date')) {
        returnPicker = flatpickr('#return-date', {
            dateFormat: 'd-m-Y',
            minDate: 'today'
        });
    }

    // toggle return date by trip type
    const oneWay = document.getElementById('oneWay');
    const roundTrip = document.getElementById('roundTrip');
    const returnWrap = document.getElementById('return-date-wrapper');
    const returnInput = document.getElementById('return-date');

    function updateTripUI() {
        if (roundTrip && roundTrip.checked) {
            returnWrap && (returnWrap.style.display = 'block');
            if (departurePicker && returnPicker) {
                const depDate = departurePicker.selectedDates[0];
                if (depDate) {
                    returnPicker.set('minDate', depDate);
                }
            }
        } else {
            returnWrap && (returnWrap.style.display = 'none');
            if (returnInput) returnInput.value = '';
        }
    }

    if (oneWay && roundTrip) {
        oneWay.addEventListener('change', updateTripUI);
        roundTrip.addEventListener('change', updateTripUI);
        updateTripUI();
    }
});


// Cuộn trang lên đầu khi nhấn nút
$('#scrollToTop').click(() => {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
});

$('.failed').click(() => {
    Swal.fire({
        icon: "warning",
        iconColor: "#ffc107",
        title: "Tính năng đang phát triển!",
        confirmButtonText: "Đóng",
        color: "#003366",
    })
})

document.addEventListener('DOMContentLoaded', function () {
    new Swiper('.swiper-container', {
        slidesPerView: 4,
        spaceBetween: 10,
        loop: true,
        autoplay: {
            delay: 3000,
            disableOnInteraction: false,
        },
        pagination: {
            el: '.swiper-pagination',
            clickable: true,
        },
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
        },
        breakpoints: {
            500: {
                slidesPerView: 1
            },
            768: {
                slidesPerView: 2,
                spaceBetween: 30,
            },
            1024: {
                slidesPerView: 4,
                spaceBetween: 40,
            },
        }
    });
});

$(document).ready( function () {
// xử lý select dropdown
    document.querySelectorAll('.departure-item').forEach(el => {
        el.addEventListener('click', event => {
            const airportCity = el.querySelector(".city-departure").textContent;
            const inputDisplay = document.getElementById('departure');
            const airportId = el.getAttribute('data-value');
            const inputId = document.getElementById('departureAirportId');
            inputDisplay.value = airportCity;
            inputId.value = airportId;
        });
    });

    document.querySelectorAll('.arrival-item').forEach(el => {
        el.addEventListener('click', event => {
            const airportCity = el.querySelector(".city-arrival").textContent;
            const inputDisplay = document.getElementById('arrival');
            const airportId = el.getAttribute('data-value');
            const inputId = document.getElementById('arrivalAirportId');
            inputDisplay.value = airportCity;
            inputId.value = airportId;
        });
    });
});

// ajax
$(document).ready(function() {
    $('.departure-item').on('click', function() {
        let departureAirportId = $(this).data('value');
        let departureText = $(this).find('.city-departure').text();

        $('#departure').val(departureText);
        $('#departureAirportId').val(departureAirportId);

        $.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            url: '/api/airports/arrival',
            type: 'GET',
            data: { departureAirportId: departureAirportId },
            success: function(data) {
                console.log(data)

                $('.arrival-dropdown-menu').empty();
                $('#arrival').val("");
                $('#arrivalAirportId').val("");


                $.each(data, function(index, airport) {
                    $('.arrival-dropdown-menu').append(
                        '<li class="dropdown-item arrival-item" data-value="' + airport.airportId + '">' +
                        '<span class="city-code">' + airport.airportCode + '</span> ' +
                        '<span class="city-arrival">' + airport.city + '</span>' +
                        '</li>'
                    );
                });


                $('.arrival-item').on('click', function() {
                    let arrivalText = $(this).find('.city-arrival').text();
                    $('#arrival').val(arrivalText);
                    $('#arrivalAirportId').val($(this).data('value'));
                });
            },
            error: function(xhr, status, error) {
                console.error('Error:', error);
            }
        });
    });
});

$(document).ready(function () {
    const form = $('form[action="/flights/search"]');
    const departureInput = $('#departureAirportId');
    const arrivalInput = $('#arrivalAirportId');
    const tripTypeInput = $('input[name="tripType"]');
    const returnDateInput = $('#return-date');

    form.on('submit', function (event) {
        let isValid = true;
        let message = "";

        if (departureInput.val() === "") {
            message += "Vui lòng chọn điểm khởi hành!\n";
            isValid = false;
        } else if (arrivalInput.val() === "") {
            message += "Vui lòng chọn điểm đến!\n";
            isValid = false;
        }

        if (tripTypeInput.filter(':checked').val() === 'roundtrip') {
            if (!returnDateInput.val()) {
                message += "Vui lòng chọn ngày về!\n";
                isValid = false;
            } else if ($('#departure-date').val() && returnDateInput.val()) {
                const [d,dM,dY] = $('#departure-date').val().split('-');
                const [r,rM,rY] = returnDateInput.val().split('-');
                const dep = new Date(+dY, +dM-1, +d);
                const ret = new Date(+rY, +rM-1, +r);
                if (ret < dep) {
                    message += "Ngày về phải sau hoặc bằng ngày đi!\n";
                    isValid = false;
                }
            }
        }

        if (!isValid) {
            toastr.options = {
                "closeButton": false,
                "debug": true,
                "newestOnTop": false,
                "progressBar": true,
                "positionClass": "toast-top-right",
                "preventDuplicates": false,
                "onclick": null,
                "showDuration": "300",
                "hideDuration": "1000",
                "timeOut": "5000",
                "extendedTimeOut": "1000",
                "showEasing": "swing",
                "hideEasing": "linear",
                "showMethod": "fadeToggle",
                "hideMethod": "fadeOut"
            }
            toastr["warning"](message)
            event.preventDefault();
        }
    });
});

$('.bookNowButton').on('click', function() {
    let targetOffset = $('#booking').offset().top;
    let windowHeight = $(window).height();
    let elementHeight = $('#booking').outerHeight();

    $('html, body').animate({
        scrollTop: targetOffset - (windowHeight / 2) + (elementHeight / 2)
    }, 100);
});

document.getElementById('btn-check-booking').addEventListener('click', function (e) {
    e.preventDefault();

    // Kiểm tra xem có đang ở trang chủ hay không
    if (window.location.pathname === '/') {
        // Cuộn đến phần "Tra Cứu" nếu đang ở trang chủ
        let targetOffset = $('#check-booking').offset().top;
        let windowHeight = $(window).height();
        let elementHeight = $('#check-booking').outerHeight();

        $('html, body').animate({
            scrollTop: targetOffset - (windowHeight / 2) + (elementHeight / 2)
        }, 100);
    } else {
        window.location.href = "/";

        $(document).ready( () => {
            let targetOffset = $('#check-booking').offset().top;
            let windowHeight = $(window).height();
            let elementHeight = $('#check-booking').outerHeight();
            $('html, body').animate({
                scrollTop: targetOffset - (windowHeight / 2) + (elementHeight / 2)
            }, 100);
        })

    }
});
