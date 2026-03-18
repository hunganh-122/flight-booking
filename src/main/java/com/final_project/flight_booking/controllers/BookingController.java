package com.final_project.flight_booking.controllers;

import com.final_project.flight_booking.models.*;
import com.final_project.flight_booking.repositories.*;
import com.final_project.flight_booking.services.FlightService;
import com.final_project.flight_booking.services.MailService;
import com.final_project.flight_booking.services.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class BookingController {

    @Autowired
    private FlightService flightService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PayMethodRepository payMethodRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private AppUserRepository userRepository;

    @PostMapping("/passenger-info")
    public String showPassengerInfo(
            @RequestParam(value = "flightId",     required = false) Integer flightId,
            @RequestParam(value = "seatIds",      required = false) List<String> seatIds,
            @RequestParam(value = "seatNumbers",  required = false) List<String> seatNumbers,
            @RequestParam(value = "totalPrice",   required = false) Double totalPrice,
            @RequestParam(value = "classType",    required = false) String classType,
            @RequestParam(value = "outboundFlightId",     required = false) Integer outboundFlightId,
            @RequestParam(value = "outboundSeatIds",      required = false) List<String> outboundSeatIds,
            @RequestParam(value = "outboundSeatNumbers",  required = false) List<String> outboundSeatNumbers,
            @RequestParam(value = "outboundTotalPrice",   required = false) Double outboundTotalPrice,
            @RequestParam(value = "outboundClassType",    required = false) String outboundClassType,
            @RequestParam(value = "returnFlightId",       required = false) Integer returnFlightId,
            @RequestParam(value = "returnSeatIds",        required = false) List<String> returnSeatIds,
            @RequestParam(value = "returnSeatNumbers",    required = false) List<String> returnSeatNumbers,
            @RequestParam(value = "returnTotalPrice",     required = false) Double returnTotalPrice,
            @RequestParam(value = "returnClassType",      required = false) String returnClassType,
            HttpSession session,
            Model model) {

        boolean isRoundTrip = (outboundFlightId != null && returnFlightId != null);

        if (isRoundTrip) {
            Flight outFlight = flightService.getFlightById(outboundFlightId);
            Flight retFlight = flightService.getFlightById(returnFlightId);
            double outp = outboundTotalPrice != null ? outboundTotalPrice : 0;
            double retp = returnTotalPrice   != null ? returnTotalPrice   : 0;
            double grandTotal = outp + retp;

            session.setAttribute("booking_roundtrip",      true);
            session.setAttribute("booking_outboundFlightId",    outboundFlightId);
            session.setAttribute("booking_outboundSeatIds",     outboundSeatIds);
            session.setAttribute("booking_outboundSeatNumbers", outboundSeatNumbers);
            session.setAttribute("booking_outboundTotalPrice",  outp);
            session.setAttribute("booking_outboundClassType",   outboundClassType);
            session.setAttribute("booking_returnFlightId",      returnFlightId);
            session.setAttribute("booking_returnSeatIds",       returnSeatIds);
            session.setAttribute("booking_returnSeatNumbers",   returnSeatNumbers);
            session.setAttribute("booking_returnTotalPrice",    retp);
            session.setAttribute("booking_returnClassType",     returnClassType);
            session.setAttribute("booking_grandTotal",          grandTotal);

            model.addAttribute("isRoundTrip", true);
            model.addAttribute("outboundFlight",       outFlight);
            model.addAttribute("outboundSeatNumbers",  outboundSeatNumbers);
            model.addAttribute("outboundClassType",    outboundClassType);
            model.addAttribute("outboundTotalPrice",   outp);
            model.addAttribute("returnFlight",         retFlight);
            model.addAttribute("returnSeatNumbers",    returnSeatNumbers);
            model.addAttribute("returnClassType",      returnClassType);
            model.addAttribute("returnTotalPrice",     retp);
            model.addAttribute("grandTotal",           grandTotal);

        } else {
            Flight flight = flightId != null ? flightService.getFlightById(flightId) : null;
            double price  = totalPrice != null ? totalPrice : 0;

            session.setAttribute("booking_roundtrip",    false);
            session.setAttribute("booking_flightId",     flightId);
            session.setAttribute("booking_seatIds",      seatIds);
            session.setAttribute("booking_seatNumbers",  seatNumbers);
            session.setAttribute("booking_totalPrice",   price);
            session.setAttribute("booking_classType",    classType);

            model.addAttribute("isRoundTrip",   false);
            model.addAttribute("flight",        flight);
            model.addAttribute("seatNumbers",   seatNumbers);
            model.addAttribute("classType",     classType);
            model.addAttribute("totalPrice",    price);
        }

        return "booking/passenger-info";
    }

    @PostMapping("/booking/proceed-payment")
    public String proceedToPayment(
            @RequestParam("passengerName")    String passengerName,
            @RequestParam("passengerEmail")   String passengerEmail,
            @RequestParam("passengerPhone")   String passengerPhone,
            @RequestParam("passengerGender")  String passengerGender,
            @RequestParam("passengerCountry") String passengerCountry,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Boolean roundTrip  = (Boolean) session.getAttribute("booking_roundtrip");
        Double  totalPrice = roundTrip != null && roundTrip
                ? (Double) session.getAttribute("booking_grandTotal")
                : (Double) session.getAttribute("booking_totalPrice");

        if (totalPrice == null || totalPrice <= 0) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu đặt vé không hợp lệ, vui lòng thử lại.");
            return "redirect:/";
        }

        session.setAttribute("passenger_name",    passengerName);
        session.setAttribute("passenger_email",   passengerEmail);
        session.setAttribute("passenger_phone",   passengerPhone);
        session.setAttribute("passenger_gender",  passengerGender);
        session.setAttribute("passenger_country", passengerCountry);

        String ipAddr = request.getHeader("X-Forwarded-For");
        if (ipAddr == null || ipAddr.isEmpty()) ipAddr = request.getRemoteAddr();

        String txnRef    = vnPayService.generateTxnRef();
        String orderInfo = "Thanh toan ve may bay - " + passengerName;

        session.setAttribute("booking_txnRef", txnRef);

        String paymentUrl = vnPayService.createPaymentUrl(totalPrice, orderInfo, txnRef, ipAddr);
        return "redirect:" + paymentUrl;
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/payment/vnpay-return")
    public String vnpayReturn(
            @RequestParam Map<String, String> params,
            HttpSession session,
            Model model) {

        boolean validSignature = vnPayService.verifyReturnUrl(params);
        String responseCode    = params.get("vnp_ResponseCode");
        String txnRef          = params.get("vnp_TxnRef");
        String amountStr       = params.get("vnp_Amount");
        String bankCode        = params.get("vnp_BankCode");
        String payDate         = params.get("vnp_PayDate");
        String transactionNo   = params.get("vnp_TransactionNo");

        boolean success = validSignature && "00".equals(responseCode);

        String passengerName    = (String) session.getAttribute("passenger_name");
        String passengerEmail   = (String) session.getAttribute("passenger_email");
        String passengerPhone   = (String) session.getAttribute("passenger_phone");
        String passengerGender  = (String) session.getAttribute("passenger_gender");
        String passengerCountry = (String) session.getAttribute("passenger_country");

        Boolean roundTrip = (Boolean) session.getAttribute("booking_roundtrip");

        double amountVnd = amountStr != null ? Long.parseLong(amountStr) / 100.0 : 0;

        model.addAttribute("success",         success);
        model.addAttribute("responseCode",    responseCode);
        model.addAttribute("txnRef",          txnRef);
        model.addAttribute("amountVnd",       amountVnd);
        model.addAttribute("bankCode",        bankCode);
        model.addAttribute("payDate",         payDate);
        model.addAttribute("transactionNo",   transactionNo);
        model.addAttribute("passengerName",   passengerName);
        model.addAttribute("passengerEmail",  passengerEmail);
        model.addAttribute("passengerPhone",  passengerPhone);
        model.addAttribute("passengerGender", passengerGender);
        model.addAttribute("passengerCountry",passengerCountry);
        model.addAttribute("isRoundTrip",     roundTrip);

        Flight outboundF = null;
        Flight returnF   = null;
        Flight oneWayF   = null;

        if (roundTrip != null && roundTrip) {
            model.addAttribute("outboundSeatNumbers", session.getAttribute("booking_outboundSeatNumbers"));
            model.addAttribute("returnSeatNumbers",   session.getAttribute("booking_returnSeatNumbers"));
            model.addAttribute("outboundClassType",   session.getAttribute("booking_outboundClassType"));
            model.addAttribute("returnClassType",     session.getAttribute("booking_returnClassType"));
            
            Integer outFid = (Integer) session.getAttribute("booking_outboundFlightId");
            Integer retFid = (Integer) session.getAttribute("booking_returnFlightId");
            if (outFid != null) {
                outboundF = flightService.getFlightById(outFid);
                model.addAttribute("outboundFlight", outboundF);
            }
            if (retFid != null) {
                returnF = flightService.getFlightById(retFid);
                model.addAttribute("returnFlight", returnF);
            }
        } else {
            model.addAttribute("seatNumbers", session.getAttribute("booking_seatNumbers"));
            model.addAttribute("classType",   session.getAttribute("booking_classType"));
            Integer fid = (Integer) session.getAttribute("booking_flightId");
            if (fid != null) {
                oneWayF = flightService.getFlightById(fid);
                model.addAttribute("flight", oneWayF);
            }
        }

        if (success) {
            User user = null;
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                user = userRepository.findByUsername(username);
            }

            PayMethod vnpayMethod = payMethodRepository.findByPaymethodName("VNPay")
                    .orElseGet(() -> {
                        PayMethod m = new PayMethod();
                        m.setPaymethodName("VNPay");
                        return payMethodRepository.save(m);
                    });

            String seatsStr = "";
            String flightInfo = "";

            if (roundTrip != null && roundTrip) {
                // Outbound
                if (outboundF != null && outboundF.getFlightId() != null && outboundF.getFlightId() < 0) {
                    outboundF = flightService.saveFlightFromAmadeus(outboundF);
                }
                
                List<String> outSeats = (List<String>) session.getAttribute("booking_outboundSeatNumbers");
                String outSeatsStr = outSeats != null ? String.join(", ", outSeats) : "";
                
                Booking b1 = new Booking();
                b1.setUser(user);
                b1.setFlight(outboundF);
                b1.setBookingDate(LocalDateTime.now());
                b1.setNumberOfSeats(outSeatsStr);
                b1.setTotalPrice((Double) session.getAttribute("booking_outboundTotalPrice"));
                b1.setStatus("CONFIRMED");
                b1.setName(passengerName);
                b1.setEmail(passengerEmail);
                b1.setGender(passengerGender);
                b1.setCountry(passengerCountry);
                b1.setCodeBooking(txnRef);
                b1.setClassType((String) session.getAttribute("booking_outboundClassType"));
                bookingRepository.save(b1);
                
                if (returnF != null && returnF.getFlightId() != null && returnF.getFlightId() < 0) {
                    returnF = flightService.saveFlightFromAmadeus(returnF);
                }
                
                List<String> retSeats = (List<String>) session.getAttribute("booking_returnSeatNumbers");
                String retSeatsStr = retSeats != null ? String.join(", ", retSeats) : "";

                Booking b2 = new Booking();
                b2.setUser(user);
                b2.setFlight(returnF);
                b2.setBookingDate(LocalDateTime.now());
                b2.setNumberOfSeats(retSeatsStr);
                b2.setTotalPrice((Double) session.getAttribute("booking_returnTotalPrice"));
                b2.setStatus("CONFIRMED");
                b2.setName(passengerName);
                b2.setEmail(passengerEmail);
                b2.setGender(passengerGender);
                b2.setCountry(passengerCountry);
                b2.setCodeBooking(txnRef);
                b2.setClassType((String) session.getAttribute("booking_returnClassType"));
                bookingRepository.save(b2);

                seatsStr = "Đi: " + outSeatsStr + " | Về: " + retSeatsStr;
                flightInfo = (outboundF != null ? outboundF.getFlightNumber() : "---") + " & " + (returnF != null ? returnF.getFlightNumber() : "---");

                Payment p = new Payment();
                p.setBooking(b1); 
                p.setPayMethod(vnpayMethod);
                p.setPaymentDate(LocalDateTime.now());
                p.setAmount(amountVnd);
                p.setPaymentStatus("SUCCESS");
                paymentRepository.save(p);

            } else {
                if (oneWayF != null && oneWayF.getFlightId() != null && oneWayF.getFlightId() < 0) {
                    oneWayF = flightService.saveFlightFromAmadeus(oneWayF);
                }

                List<String> sList = (List<String>) session.getAttribute("booking_seatNumbers");
                String sStr = sList != null ? String.join(", ", sList) : "";

                Booking b = new Booking();
                b.setUser(user);
                b.setFlight(oneWayF);
                b.setBookingDate(LocalDateTime.now());
                b.setNumberOfSeats(sStr);
                b.setTotalPrice((Double) session.getAttribute("booking_totalPrice"));
                b.setStatus("CONFIRMED");
                b.setName(passengerName);
                b.setEmail(passengerEmail);
                b.setGender(passengerGender);
                b.setCountry(passengerCountry);
                b.setCodeBooking(txnRef);
                b.setClassType((String) session.getAttribute("booking_classType"));
                bookingRepository.save(b);

                seatsStr = sStr;
                flightInfo = (oneWayF != null ? oneWayF.getFlightNumber() : "---");

                Payment p = new Payment();
                p.setBooking(b);
                p.setPayMethod(vnpayMethod);
                p.setPaymentDate(LocalDateTime.now());
                p.setAmount(amountVnd);
                p.setPaymentStatus("SUCCESS");
                paymentRepository.save(p);
            }

            try {
                mailService.sendTicketEmail(passengerEmail, passengerName, flightInfo, seatsStr, amountVnd, txnRef);
            } catch (Exception e) {
                System.err.println("Error sending email: " + e.getMessage());
            }

            session.removeAttribute("booking_roundtrip");
            session.removeAttribute("booking_flightId");
            session.removeAttribute("booking_seatIds");
            session.removeAttribute("booking_seatNumbers");
            session.removeAttribute("booking_totalPrice");
            session.removeAttribute("booking_classType");
            session.removeAttribute("booking_outboundFlightId");
            session.removeAttribute("booking_outboundSeatIds");
            session.removeAttribute("booking_outboundSeatNumbers");
            session.removeAttribute("booking_outboundTotalPrice");
            session.removeAttribute("booking_outboundClassType");
            session.removeAttribute("booking_returnFlightId");
            session.removeAttribute("booking_returnSeatIds");
            session.removeAttribute("booking_returnSeatNumbers");
            session.removeAttribute("booking_returnTotalPrice");
            session.removeAttribute("booking_returnClassType");
            session.removeAttribute("booking_grandTotal");
            session.removeAttribute("booking_txnRef");
            session.removeAttribute("passenger_name");
            session.removeAttribute("passenger_email");
            session.removeAttribute("passenger_phone");
            session.removeAttribute("passenger_gender");
            session.removeAttribute("passenger_country");
        }

        return "booking/payment-result";
    }
}
