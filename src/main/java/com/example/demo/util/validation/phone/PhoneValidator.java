package com.example.demo.util.validation.phone;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<PhoneNumber, String> {

    // PhoneNumber: Annotation đánh dấu các field cần validate
    // String: Kiểu dữ liệu của giá trị đầu vào (ở đây là số điện thoại cần kiểm tra)

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }

        // validate phone numbers of format "0902345345"
        if (phoneNumber.matches("\\d{10}"))
            return true;
        // validating phone number with -, . or spaces: 090-234-4567
        else if (phoneNumber.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}"))
            return true;
        // validating phone number with extension length from 3 to 5
        else // return false if nothing matches the input
        if (phoneNumber.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}"))
            return true;
        // validating phone number where area code is in braces ()
        else
            return phoneNumber.matches("\\(\\d{3}\\)-\\d{3}-\\d{4}");
    }

}
