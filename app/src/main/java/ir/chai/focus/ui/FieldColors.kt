package ir.chai.focus.ui

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable

/**
 * رنگ‌های یکدست فیلدهای متنی — بدون این، متریال رنگ‌های پیش‌فرض خودش
 * (بنفش/خاکستری‌های ناخوانا در تم تیره) را می‌گذارد و «طیف رنگ» فیلد به‌هم می‌ریزد.
 */
@Composable
fun chaiFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Chai.Text,
    unfocusedTextColor = Chai.Text,
    errorTextColor = Chai.Text,
    focusedContainerColor = Chai.Surface,
    unfocusedContainerColor = Chai.Surface,
    errorContainerColor = Chai.Surface,
    cursorColor = Chai.Primary,
    focusedBorderColor = Chai.Primary,
    unfocusedBorderColor = Chai.Line,
    errorBorderColor = Chai.Danger,
    focusedLabelColor = Chai.Primary,
    unfocusedLabelColor = Chai.Text2,
    errorLabelColor = Chai.Danger,
    focusedPlaceholderColor = Chai.Text2,
    unfocusedPlaceholderColor = Chai.Text2,
    focusedSupportingTextColor = Chai.Text2,
    unfocusedSupportingTextColor = Chai.Text2,
    errorSupportingTextColor = Chai.Danger,
    focusedTrailingIconColor = Chai.Text2,
    unfocusedTrailingIconColor = Chai.Text2,
    errorTrailingIconColor = Chai.Danger
)
