package com.london.excer1cal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.london.excer1cal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var textViewResult: TextView
    private lateinit var binding: ActivityMainBinding
    private var currentExpression = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textViewResult = findViewById(R.id.tv_num)

        // Asigna los listeners a los botones
        binding.btnOne.setOnClickListener(this)
        binding.btnZero.setOnClickListener(this)
        binding.btnTwo.setOnClickListener(this)
        binding.btnThree.setOnClickListener(this)
        binding.btnFour.setOnClickListener(this)
        binding.btnFive.setOnClickListener(this)
        binding.btnSix.setOnClickListener(this)
        binding.btnSeven.setOnClickListener(this)
        binding.btnEight.setOnClickListener(this)
        binding.btnNine.setOnClickListener(this)
        binding.btnPareder.setOnClickListener(this)
        binding.btnPareiz.setOnClickListener(this)
        binding.btnDelet.setOnClickListener { deleteLastNumber() }
        binding.btnDeletAll.setOnClickListener { clearNumbers() }
        binding.btnPunt.setOnClickListener { appendDot() }
        binding.btnSum.setOnClickListener { performOperations("+") }
        binding.btnRest.setOnClickListener { performOperations("-") }
        binding.btnMult.setOnClickListener { performOperations("*") }
        binding.btnDiv.setOnClickListener { performOperations("/") }
        binding.btnIgual.setOnClickListener { performEqual() }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_zero -> appendNumber("0")
            R.id.btn_one -> appendNumber("1")
            R.id.btn_two -> appendNumber("2")
            R.id.btn_three -> appendNumber("3")
            R.id.btn_four -> appendNumber("4")
            R.id.btn_five -> appendNumber("5")
            R.id.btn_six -> appendNumber("6")
            R.id.btn_seven -> appendNumber("7")
            R.id.btn_eight -> appendNumber("8")
            R.id.btn_nine -> appendNumber("9")
            R.id.btn_pareder -> performOperations(")")
            R.id.btn_pareiz -> performOperations("(")
        }
    }

    private fun appendNumber(number: String) {
        currentExpression += number
        textViewResult.text = currentExpression
    }

    private fun deleteLastNumber() {
        if (currentExpression.isNotEmpty()) {
            currentExpression = currentExpression.substring(0, currentExpression.length - 1)
            textViewResult.text = currentExpression
        }
    }

    private fun clearNumbers() {
        currentExpression = ""
        textViewResult.text = ""
    }

    private fun appendDot() {
        val currentText = textViewResult.text.toString()
        if (!currentText.contains(".")) {
            textViewResult.text = "$currentText."
        }
    }

    private fun performOperations(operation: String) {
        when {
            operation == "(" -> {
                currentExpression += operation
                textViewResult.text = currentExpression
            }
            operation == ")" -> {
                if (currentExpression.count { it == '(' } > currentExpression.count { it == ')' }) {
                    val lastOpenParenthesisIndex = currentExpression.lastIndexOf('(')
                    if (lastOpenParenthesisIndex != -1) {
                        val subexpression = currentExpression.substring(lastOpenParenthesisIndex + 1)
                        try {
                            val result = evaluateExpression(subexpression)
                            currentExpression = currentExpression.substring(0, lastOpenParenthesisIndex) + result.toString()
                            textViewResult.text = currentExpression
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error evaluating subexpression: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "No corresponding open parenthesis found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No open parentheses", Toast.LENGTH_SHORT).show()
                }
            }
            "+-*/".contains(operation) -> {
                if (currentExpression.isNotEmpty()) {
                    // Reemplazar la última operación si existe
                    if ("+-*/".contains(currentExpression.last())) {
                        currentExpression = currentExpression.substring(0, currentExpression.length - 1)
                    }
                    currentExpression += operation
                    textViewResult.text = currentExpression
                }
            }
            operation.matches(Regex("\\d")) -> {
                currentExpression += operation
                textViewResult.text = currentExpression
            }
        }
    }

    private fun performEqual() {
        try {
            val inputExpression = currentExpression.trim()

            if (inputExpression.isNotEmpty()) {
                if (areParenthesesBalanced(inputExpression)) {
                    val result = evaluateExpression(inputExpression)
                    currentExpression = result.toString()
                    textViewResult.text = result.toString()
                } else {
                    throw Exception("Paréntesis desequilibrados")
                }
            } else {
                throw Exception("Expresión vacía")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun evaluateExpression(expression: String): Double {
        val parts = expression.split(Regex("(?=[+\\-*/()])|(?<=[+\\-*/()])"))
        val stackNumbers = mutableListOf<Double>()
        val stackOperators = mutableListOf<String>()
        val stackParentheses = mutableListOf<String>()

        for (part in parts) {
            when {
                part.matches(Regex("[0-9]+(\\.[0-9]+)?")) -> {
                    val number = part.toDouble()
                    if (stackParentheses.isEmpty()) {
                        stackNumbers.add(number)
                    } else {
                        stackParentheses.add(part)
                    }
                }
                "*/".contains(part) -> {
                    if (stackParentheses.isEmpty()) {
                        while (stackOperators.isNotEmpty() && "*/".contains(stackOperators.last())) {
                            performOperation(stackNumbers, stackOperators)
                        }
                        stackOperators.add(part)
                    } else {
                        stackOperators.add(part)
                    }
                }
                "+-".contains(part) -> {
                    if (stackParentheses.isEmpty()) {
                        while (stackOperators.isNotEmpty() && "+-".contains(stackOperators.last())) {
                            performOperation(stackNumbers, stackOperators)
                        }
                        stackOperators.add(part)
                    } else {
                        while (stackOperators.isNotEmpty() && "*/".contains(stackOperators.last())) {
                            performOperation(stackNumbers, stackOperators)
                        }
                        stackOperators.add(part)
                    }
                }
                part == "(" -> {
                    stackParentheses.add(part)
                }
                part == ")" -> {
                    while (stackOperators.isNotEmpty() && stackOperators.last() != "(") {
                        performOperation(stackNumbers, stackOperators)
                    }
                    if (stackOperators.isNotEmpty() && stackOperators.last() == "(") {
                        stackOperators.removeAt(stackOperators.size - 1)
                    } else {
                        throw Exception("Paréntesis desequilibrados")
                    }

                    // Evaluar operaciones dentro de los paréntesis de izquierda a derecha
                    while (stackParentheses.isNotEmpty()) {
                        val subexpression = stackParentheses.removeAt(stackParentheses.size - 1)
                        val subresult = evaluateExpression(subexpression)
                        stackNumbers.add(subresult)
                    }
                }
                else -> throw Exception("Operación inválida")
            }
        }

        while (stackOperators.isNotEmpty()) {
            performOperation(stackNumbers, stackOperators)
        }

        if (stackNumbers.size == 1 && stackOperators.isEmpty()) {
            return stackNumbers[0]
        } else {
            throw Exception("Expresión incorrecta")
        }
    }


    private fun hasHigherOrEqualPrecedence(op1: String, op2: String): Boolean {
        val precedenceMap = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2)
        return precedenceMap[op1] ?: 0 >= precedenceMap[op2] ?: 0
    }

    private fun performOperation(stackNumbers: MutableList<Double>, stackOperators: MutableList<String>) {
        if (stackNumbers.size < 2 || stackOperators.isEmpty()) {
            throw Exception("La pila no tiene suficientes operandos")
        }

        val operand2 = stackNumbers.removeAt(stackNumbers.size - 1)
        val operand1 = stackNumbers.removeAt(stackNumbers.size - 1)
        val operator = stackOperators.removeAt(stackOperators.size - 1)

        if (operator != "(") {
            when (operator) {
                "+" -> stackNumbers.add(operand1 + operand2)
                "-" -> stackNumbers.add(operand1 - operand2)
                "*" -> stackNumbers.add(operand1 * operand2)
                "/" -> {
                    if (operand2 == 0.0) {
                        throw Exception("División por cero")
                    }
                    stackNumbers.add(operand1 / operand2)
                }
                else -> throw Exception("Operador desconocido")
            }
        }
    }
    private fun areParenthesesBalanced(expression: String): Boolean {
        val stack = mutableListOf<Char>()
        for (char in expression) {
            if (char == '(') {
                stack.add(char)
            } else if (char == ')') {
                if (stack.isEmpty() || stack.removeAt(stack.size - 1) != '(') {
                    return false
                }
            }
        }
        return stack.isEmpty()
    }
}
