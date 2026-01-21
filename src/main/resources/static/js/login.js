
    function myMenuFunction() {
    var i = document.getElementById("navMenu");

    if (i.className === "nav-menu") {
    i.className += " responsive";
} else {
    i.className = "nav-menu";
}
}



    var a = document.getElementById("loginBtn");
    var b = document.getElementById("registerBtn");
    var x = document.getElementById("login");
    var y = document.getElementById("register");

    function login() {
    x.style.left = "4px";
    y.style.right = "-520px";
    a.className += " white-btn";
    b.className = "btn";
    x.style.opacity = 1;
    y.style.opacity = 0;
}

    function register() {
    x.style.left = "-510px";
    y.style.right = "5px";
    a.className = "btn";
    b.className += " white-btn";
    x.style.opacity = 0;
    y.style.opacity = 1;
}

// OTP Registration Logic
$(document).ready(function() {
    let otpTimer = null;
    let timeLeft = 300;
    let isOtpVerified = false;
    
    $('#registerSubmitBtn').on('click', function(e) {
        e.preventDefault();
        
        if (!validateRegistrationForm()) {
            return;
        }
        
        const username = $('#username').val();
        const email = $('#email').val();
        
        // Disable button and show loading state
        $('#registerSubmitBtn').prop('disabled', true).text('Đang kiểm tra...');
        
        console.log('Sending check availability request for:', { username, email });
        
        $.ajax({
            url: '/api/check-availability',
            method: 'POST',
            data: { 
                username: username,
                email: email
            },
            success: function(response) {
                console.log('Response received:', response);
                if (response.success) {
                    $('#otpSection').slideDown();
                    $('#registerSubmitBtn').hide();
                    $('#verifyBtn').show();
                    
                    const formBox = document.querySelector('.form-box');
                    formBox.classList.add('expanded');
                    
                    $('html, body').animate({
                        scrollTop: $('#otpSection').offset().top - 100
                    }, 500);
                    
                    toastr.success('Thông tin hợp lệ! Vui lòng xác thực email.');
                } else {
                    toastr.error(response.message);
                    console.log("error: " + response.message);
                    if (response.field === 'email') {
                        $('#email').focus();
                    } else if (response.field === 'username') {
                        $('#username').focus();
                    } else if (response.field === 'both') {
                        $('#username').focus();
                    }
                    $('#registerSubmitBtn').prop('disabled', false).text('Đăng Ký');
                }
            },
            error: function(xhr, status, error) {
                console.log("AJAX error:", { xhr, status, error });
                toastr.error('Có lỗi xảy ra khi kiểm tra thông tin');
                $('#registerSubmitBtn').prop('disabled', false).text('Đăng Ký');
            }
        });
    });
    
    $('#sendOtpBtn').on('click', function() {
        const email = $('#email').val();
        if (!email || !isValidEmail(email)) {
            toastr.error('Vui lòng nhập email hợp lệ');
            return;
        }
        
        $(this).prop('disabled', true);
        $(this).text('Đang gửi...');
        
        $.ajax({
            url: '/api/otp/send',
            method: 'POST',
            data: { email: email },
            success: function(response) {
                if (response.success) {
                    toastr.success(response.message);
                    $('#sendOtpBtn').hide();
                    $('#resendOtpBtn').show();
                    $('#otpTimer').show();
                    startTimer();
                } else {
                    toastr.error(response.message);
                    $('#sendOtpBtn').prop('disabled', false);
                    $('#sendOtpBtn').text('Gửi mã OTP');
                }
            },
            error: function() {
                toastr.error('Có lỗi xảy ra khi gửi OTP');
                $('#sendOtpBtn').prop('disabled', false);
                $('#sendOtpBtn').text('Gửi mã OTP');
            }
        });
    });
    
    $('#resendOtpBtn').on('click', function() {
        const email = $('#email').val();
        
        $(this).prop('disabled', true);
        $(this).text('Đang gửi...');
        
        $.ajax({
            url: '/api/otp/resend',
            method: 'POST',
            data: { email: email },
            success: function(response) {
                if (response.success) {
                    toastr.success(response.message);
                    startTimer();
                } else {
                    toastr.error(response.message);
                }
                $('#resendOtpBtn').prop('disabled', false);
                $('#resendOtpBtn').text('Gửi lại');
            },
            error: function() {
                toastr.error('Có lỗi xảy ra khi gửi lại OTP');
                $('#resendOtpBtn').prop('disabled', false);
                $('#resendOtpBtn').text('Gửi lại');
            }
        });
    });
    
    $('#otpCode').on('input', function() {
        const otpCode = $(this).val();
        if (otpCode.length === 6) {
            verifyOtp();
        } else {
            isOtpVerified = false;
            $('#verifyBtn').prop('disabled', true);
        }
    });
    
    function verifyOtp() {
        const email = $('#email').val();
        const otpCode = $('#otpCode').val();
        
        if (!email || !otpCode) return;
        
        $.ajax({
            url: '/api/otp/check',
            method: 'POST',
            data: { 
                email: email,
                code: otpCode
            },
            success: function(response) {
                if (response.success) {
                    toastr.success('Mã OTP hợp lệ! Bấm "Xác Thực và Hoàn Tất" để hoàn tất đăng ký.');
                    isOtpVerified = true;
                    $('#verifyBtn').prop('disabled', false);
                    $('#otpCode').css('border-color', '#28a745');
                } else {
                    toastr.error(response.message);
                    isOtpVerified = false;
                    $('#verifyBtn').prop('disabled', true);
                    $('#otpCode').css('border-color', '#dc3545');
                }
            },
            error: function() {
                toastr.error('Có lỗi xảy ra khi xác thực OTP');
                isOtpVerified = false;
                $('#verifyBtn').prop('disabled', true);
            }
        });
    }
    
    // Final verification and registration
    $('#verifyBtn').on('click', function(e) {
        e.preventDefault();
        
        if (!isOtpVerified) {
            toastr.error('Vui lòng nhập và xác thực mã OTP');
            return;
        }
                if (timeLeft <= 0) {
            toastr.error('Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.');
            return;
        }
                $('#registerForm').submit();
    });
    
    // Timer function
    function startTimer() {
        timeLeft = 300;
        updateTimerDisplay();
        
        if (otpTimer) clearInterval(otpTimer);
        
        otpTimer = setInterval(function() {
            timeLeft--;
            updateTimerDisplay();
            
            if (timeLeft <= 0) {
                clearInterval(otpTimer);
                $('#otpTimer').hide();
                isOtpVerified = false;
                $('#verifyBtn').prop('disabled', true);
                toastr.warning('Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.');
            }
        }, 1000);
    }
    
    function updateTimerDisplay() {
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        $('#countdown').text(minutes + ':' + (seconds < 10 ? '0' : '') + seconds);
    }
    
    // Email validation
    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }
    
    // Validate registration form
    function validateRegistrationForm() {
        const fullname = $('#fullname').val();
        const username = $('#username').val();
        const email = $('#email').val();
        const password = $('#password').val();
        const rePassword = $('#rePassword').val();
        
        if (!fullname) {
            toastr.error('Vui lòng nhập tên đầy đủ');
            return false;
        }
        
        if (!username) {
            toastr.error('Vui lòng nhập tên đăng nhập');
            return false;
        }
        
        if (!email || !isValidEmail(email)) {
            toastr.error('Vui lòng nhập email hợp lệ');
            return false;
        }
        
        if (!password) {
            toastr.error('Vui lòng nhập mật khẩu');
            return false;
        }
        
        if (password.length < 6) {
            toastr.error('Mật khẩu phải có ít nhất 6 ký tự');
            return false;
        }
        
        if (password !== rePassword) {
            toastr.error('Mật khẩu nhập lại không khớp');
            return false;
        }
        
        return true;
    }
    
    // Form validation on submit
    $('#registerForm').on('submit', function(e) {
        if (!isOtpVerified) {
            e.preventDefault();
            toastr.error('Vui lòng nhập và xác thực mã OTP');
            return false;
        }
        
        if (timeLeft <= 0) {
            e.preventDefault();
            toastr.error('Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.');
            return false;
        }
    });
    
    $('#loginBtn').on('click', function() {
        if (otpTimer) {
            clearInterval(otpTimer);
        }
    });
});
