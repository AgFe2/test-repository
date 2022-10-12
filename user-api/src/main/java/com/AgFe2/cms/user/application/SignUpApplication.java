package com.AgFe2.cms.user.application;

import com.AgFe2.cms.user.client.MailgunClient;
import com.AgFe2.cms.user.client.mailgun.SendMailForm;
import com.AgFe2.cms.user.domain.SignUpForm;
import com.AgFe2.cms.user.domain.model.Customer;
import com.AgFe2.cms.user.exception.CustomException;
import com.AgFe2.cms.user.exception.ErrorCode;
import com.AgFe2.cms.user.service.SignUpCustomerService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SignUpApplication {
    private final MailgunClient mailgunClient;
    private final SignUpCustomerService signUpCustomerService;

    public String customerSignUp(SignUpForm form) {
        if (signUpCustomerService.isEmailExist(form.getEmail())) {
            // exception
            throw new CustomException(ErrorCode.ALREADY_REGISTER_USER);
        } else {
            Customer c = signUpCustomerService.signUp(form);
            LocalDateTime now = LocalDateTime.now();

            String code = getRandomCode();
            SendMailForm sendMailForm = SendMailForm.builder()
                    .from("tester@mytester.com")
                    .to(c.getEmail())
                    .subject("Verification Email!")
                    .text(getVerificationEmailBody(c.getEmail(), c.getName(), code))
                    .build();
            mailgunClient.sendEmail(sendMailForm);
            signUpCustomerService.changeCustomerValidateEmail(c.getId(), code);
            return "회원 가입에 성공하였습니다.";
        }


    }

    private String getRandomCode() {
        return RandomStringUtils.random(10,true,true);
    }

    private String getVerificationEmailBody(String email, String name, String code) {
        StringBuilder builder = new StringBuilder();
        return builder.append("Hello ").append(name).append("! Please Click Link for verification.\n\n")
                .append("http://localhost:8080/customer/signup/verify?email=")
                .append(email)
                .append("&code=")
                .append("code").toString();
    }
}
