package com.example.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    Button btnMenu;
    TextView textviewHistory, textviewResult;
    String phepTinh = "";
    String ketQua = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ánh xạ nút "Menu" từ layout
        btnMenu = findViewById(R.id.btnback);

        // Ánh xạ các TextView từ layout
        textviewHistory = findViewById(R.id.textviewHistory);
        textviewResult = findViewById(R.id.textviewResult);


        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển từ MainActivity về MenuActivity
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });

        setUpButtons();
    }

    private void setUpButtons() {
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btncong, R.id.btntru, R.id.btnnhan, R.id.btnchia, R.id.btnmod,
                R.id.btnsin, R.id.btncos, R.id.btntan, R.id.btncot,
                R.id.btnthapphan, R.id.btnamduong, R.id.btnac, R.id.btndel, R.id.btnbang
        };

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                String buttonText = b.getText().toString();
                xuLyButton(buttonText);
            }
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(listener);
        }
    }

    private void xuLyButton(String buttonText) {
        switch (buttonText) {
            case "Ac":
                phepTinh = "";
                ketQua = "";
                break;
            case "Del":
                if (!phepTinh.isEmpty()) {
                    phepTinh = phepTinh.substring(0, phepTinh.length() - 1);
                }
                break;
            case "=":
                ketQua = tinhKetQua(phepTinh);
                break;
            case "+/-":
                toggleSign();
                break;
            default:
                phepTinh += buttonText;
                break;
        }
        textviewHistory.setText(phepTinh);
        textviewResult.setText(ketQua);
    }

    private void toggleSign() {
        int len = phepTinh.length();
        if (len == 0) return;

        // Tìm số cuối cùng trong biểu thức
        int i = len - 1;
        while (i >= 0 && (Character.isDigit(phepTinh.charAt(i)) || phepTinh.charAt(i) == '.')) {
            i--;
        }

        // Nếu số đã âm
        if (i >= 0 && phepTinh.charAt(i) == '-') {
            // Bỏ dấu âm
            phepTinh = phepTinh.substring(0, i) + phepTinh.substring(i + 1);
        } else {
            // Ngược lại thì thêm dấu âm
            phepTinh = phepTinh.substring(0, i + 1) + '-' + phepTinh.substring(i + 1);
        }
    }

    private String tinhKetQua(String phepTinh) {
        try {
            return String.valueOf(eval(phepTinh));
        } catch (Exception e) {
            return "Lỗi";
        }
    }

    private double eval(String expression) {
        char[] tokens = expression.toCharArray();

        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();
        boolean expectOperand = true;  // Để xử lý điểm trừ đơn nhất

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == ' ')
                continue;

            if ((tokens[i] >= '0' && tokens[i] <= '9') || tokens[i] == '.') {
                StringBuilder sbuf = new StringBuilder();
                while (i < tokens.length && ((tokens[i] >= '0' && tokens[i] <= '9') || tokens[i] == '.'))
                    sbuf.append(tokens[i++]);
                values.push(Double.parseDouble(sbuf.toString()));
                expectOperand = false;
                i--;
            } else if (tokens[i] == '(') {
                ops.push(tokens[i]);
                expectOperand = true;
            } else if (tokens[i] == ')') {
                while (ops.peek() != '(')
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                ops.pop();
                expectOperand = false;
            } else if (tokens[i] == '+' || tokens[i] == '-' || tokens[i] == '*' || tokens[i] == '/' || tokens[i] == '%' ||
                    tokens[i] == 's' || tokens[i] == 'c' || tokens[i] == 't') {
                if (tokens[i] == '-' && expectOperand) {
                    StringBuilder sbuf = new StringBuilder();
                    sbuf.append(tokens[i++]);
                    while (i < tokens.length && ((tokens[i] >= '0' && tokens[i] <= '9') || tokens[i] == '.'))
                        sbuf.append(tokens[i++]);
                    values.push(Double.parseDouble(sbuf.toString()));
                    expectOperand = false;
                    i--;
                } else {
                    while (!ops.empty() && hasPrecedence(tokens[i], ops.peek()))
                        values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                    ops.push(tokens[i]);
                    expectOperand = true;
                }
            }
        }

        while (!ops.empty())
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));

        return values.pop();
    }

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')')
            return false;
        if ((op1 == '*' || op1 == '/' || op1 == '%') && (op2 == '+' || op2 == '-'))
            return false;
        else
            return true;
    }

    private double applyOp(char op, double b, double a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0)
                    throw new UnsupportedOperationException("Không thể chia cho số 0");
                return a / b;
            case '%':
                return a % b;
            case 's':
                return Math.sin(Math.toRadians(b));
            case 'c':
                return Math.cos(Math.toRadians(b));
            case 't':
                return Math.tan(Math.toRadians(b));
        }
        return 0;
    }
}