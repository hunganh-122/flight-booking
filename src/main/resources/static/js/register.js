$(document).ready(function() {
    let timer;
    let countdownMinutes = 5;
    let countdownSeconds = 0;

    $('#registerBtn').click(function() {
        const username = $('#username').val();
        const email = $('#email').val();
        const password = $('#password').val();
        const rePassword = $('#rePassword').val();
        const fullname = $('#fullname').val();

        if (!username || !email || !password || !rePassword || !fullname) {
            toastr.error('Vui lòng điền đầy đủ thông tin!');
            return;
        }

        if (password !== rePassword) {
            toastr.error('Mật khẩu nhập lại không khớp!');
            return;
        }

        $.post('/api/check-availability', { username: username, email: email }, function(response) {
            if (response.success) {
                sendOtp(email);
            } else {
                toastr.error(response.message);
            }
        });
    });

    function sendOtp(email) {
        $.post('/api/otp/send', { email: email }, function(response) {
            if (response.success) {
                toastr.success(response.message);
                $('#otpSection').fadeIn();
                $('#registerBtn').hide();
                $('#verifyBtn').show();
                startTimer();
            } else {
                toastr.error(response.message);
            }
        });
    }

    $('#resendOtpBtn').click(function() {
        const email = $('#email').val();
        sendOtp(email);
    });

    function startTimer() {
        clearInterval(timer);
        countdownMinutes = 5;
        countdownSeconds = 0;
        
        timer = setInterval(function() {
            if (countdownSeconds === 0) {
                if (countdownMinutes === 0) {
                    clearInterval(timer);
                    toastr.warning('Mã OTP đã hết hạn. Vui lòng gửi lại.');
                    return;
                }
                countdownMinutes--;
                countdownSeconds = 59;
            } else {
                countdownSeconds--;
            }
            
            let displayMinutes = countdownMinutes < 10 ? '0' + countdownMinutes : countdownMinutes;
            let displaySeconds = countdownSeconds < 10 ? '0' + countdownSeconds : countdownSeconds;
            $('#countdown').text(displayMinutes + ':' + displaySeconds);
        }, 1000);
    }
    
    $('#verifyBtn').on('click', function(e) {
        const otpCode = $('#otpCode').val();
        if (!otpCode || otpCode.length !== 6) {
            e.preventDefault();
            toastr.error('Vui lòng nhập mã OTP 6 chữ số!');
        }
    });
});
