package com.final_project.flight_booking.controllers;

import com.final_project.flight_booking.models.Flight;
import com.final_project.flight_booking.services.FlightService;
import com.final_project.flight_booking.services.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class BookingController {

    @Autowired
    private FlightService flightService;

    @Autowired
    private VNPayService vnPayService;

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

        if (roundTrip != null && roundTrip) {
            model.addAttribute("outboundSeatNumbers", session.getAttribute("booking_outboundSeatNumbers"));
            model.addAttribute("returnSeatNumbers",   session.getAttribute("booking_returnSeatNumbers"));
            model.addAttribute("outboundClassType",   session.getAttribute("booking_outboundClassType"));
            model.addAttribute("returnClassType",     session.getAttribute("booking_returnClassType"));
            
            Integer outFid = (Integer) session.getAttribute("booking_outboundFlightId");
            Integer retFid = (Integer) session.getAttribute("booking_returnFlightId");
            if (outFid != null) model.addAttribute("outboundFlight", flightService.getFlightById(outFid));
            if (retFid != null) model.addAttribute("returnFlight",   flightService.getFlightById(retFid));
        } else {
            model.addAttribute("seatNumbers", session.getAttribute("booking_seatNumbers"));
            model.addAttribute("classType",   session.getAttribute("booking_classType"));
            Integer fid = (Integer) session.getAttribute("booking_flightId");
            if (fid != null) model.addAttribute("flight", flightService.getFlightById(fid));
        }

        if (success) {
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
