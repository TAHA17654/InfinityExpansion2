package com.balugaq.netex.api.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Calculator {
   private static final Map<String, Integer> PRIORITY = new HashMap<>();

   public static BigDecimal calculate(String expression) throws NumberFormatException {
      if (expression != null && !expression.trim().isEmpty()) {
         String expr = replaceBrackets(expression);
         expr = replaceUnits(expr);
         expr = expr.replaceAll("\\s+", "");
         expr = expr.replaceAll("_", "");
         expr = completeParentheses(expr);
         if (!isValidParentheses(expr)) {
            throw new NumberFormatException("Invalid expression");
         } else {
            Deque<BigDecimal> numStack = new ArrayDeque<>();
            Deque<String> opStack = new ArrayDeque<>();
            int i = 0;
            int n = expr.length();

            while (i < n) {
               char c = expr.charAt(i);
               if (Character.isDigit(c) || c == '.') {
                  int j = readNumberEnd(expr, i);
                  boolean isPercentage = false;
                  if (j < n && expr.charAt(j) == '%') {
                     isPercentage = true;
                     j++;
                  }

                  String numStr = expr.substring(i, j - (isPercentage ? 1 : 0));
                  BigDecimal num = parseNumber(numStr);
                  if (isPercentage) {
                     num = num.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
                  }

                  numStack.push(num);
                  i = j;
               } else if (c == '(') {
                  opStack.push(String.valueOf(c));
                  i++;
               } else if (c == ')') {
                  String pk = opStack.peek();
                  if (pk == null) {
                     throw new NumberFormatException("Brackets doesn't match: " + expression);
                  }

                  while (!pk.equals("(")) {
                     calculateTop(numStack, opStack);
                  }

                  opStack.pop();
                  i++;
               } else if ((c == '<' || c == '>') && i + 1 < n && expr.charAt(i + 1) == c) {
                  String op = expr.substring(i, i + 2);

                  while (!opStack.isEmpty() && getPriority(opStack.peek()) >= getPriority(op)) {
                     calculateTop(numStack, opStack);
                  }

                  opStack.push(op);
                  i += 2;
               } else if (c != '~' && c != '!') {
                  if (!isOperator(String.valueOf(c))) {
                     throw new NumberFormatException("Invalid character: " + c);
                  }

                  if (c != '+' || i != 0 && expr.charAt(i - 1) != '(' && !isOperator(String.valueOf(expr.charAt(i - 1)))) {
                     if (c != '-' || i != 0 && expr.charAt(i - 1) != '(' && !isOperator(String.valueOf(expr.charAt(i - 1)))) {
                        String op = String.valueOf(c);

                        while (!opStack.isEmpty() && getPriority(opStack.peek()) >= getPriority(op)) {
                           calculateTop(numStack, opStack);
                        }

                        opStack.push(op);
                        i++;
                     } else {
                        if (i + 1 >= n || !Character.isDigit(expr.charAt(i + 1)) && expr.charAt(i + 1) != '.') {
                           throw new NumberFormatException("Invalid negative signature: " + expression);
                        }

                        int jx = readNumberEnd(expr, i + 1);
                        boolean isPercentagex = false;
                        if (jx < n && expr.charAt(jx) == '%') {
                           isPercentagex = true;
                           jx++;
                        }

                        String numStr = "-" + expr.substring(i + 1, jx - (isPercentagex ? 1 : 0));
                        BigDecimal num = parseNumber(numStr);
                        if (isPercentagex) {
                           num = num.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
                        }

                        numStack.push(num);
                        i = jx;
                     }
                  } else {
                     if (i + 1 >= n || !Character.isDigit(expr.charAt(i + 1)) && expr.charAt(i + 1) != '.') {
                        throw new NumberFormatException("Invalid positive signature: " + expression);
                     }

                     int jxx = readNumberEnd(expr, i + 1);
                     boolean isPercentagexx = false;
                     if (jxx < n && expr.charAt(jxx) == '%') {
                        isPercentagexx = true;
                        jxx++;
                     }

                     String numStr = expr.substring(i + 1, jxx - (isPercentagexx ? 1 : 0));
                     BigDecimal num = parseNumber(numStr);
                     if (isPercentagexx) {
                        num = num.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
                     }

                     numStack.push(num);
                     i = jxx;
                  }
               } else {
                  String op = String.valueOf(c);
                  opStack.push(op);
                  i++;
               }
            }

            while (!opStack.isEmpty()) {
               calculateTop(numStack, opStack);
            }

            if (numStack.size() != 1) {
               throw new NumberFormatException("Invalid expression: " + expression);
            } else {
               return numStack.pop();
            }
         }
      } else {
         throw new NumberFormatException("Empty expression");
      }
   }

   private static String replaceBrackets(String expr) {
      return expr.replaceAll("[\\[\\{\uff08\u3010\u300a]", "(").replaceAll("[\\]\\}\uff09\u3011\u300b]", ")");
   }

   private static String replaceUnits(String expr) {
      return expr.replaceAll("[Hh]", "*100")
         .replaceAll("[Kk]", "*1000")
         .replaceAll("[Ww]", "*10000")
         .replaceAll("[Mm]", "*1_000_000")
         .replaceAll("[Bb]", "*1_000_000_000")
         .replaceAll("[Tt]", "*1_000_000_000_000")
         .replaceAll("[Qq]", "*1_000_000_000_000_000");
   }

   private static String completeParentheses(String expr) {
      int openCount = 0;
      int closeCount = 0;

      for (char c : expr.toCharArray()) {
         if (c == '(') {
            openCount++;
         } else if (c == ')') {
            closeCount++;
         }
      }

      int diff = openCount - closeCount;
      if (diff > 0) {
         expr = expr + ")".repeat(diff);
      } else if (diff < 0) {
         expr = "(".repeat(Math.max(0, -diff)) + expr;
      }

      return expr;
   }

   private static int readNumberEnd(String expr, int start) {
      int j = start;
      int n = expr.length();

      while (j < n && (Character.isDigit(expr.charAt(j)) || expr.charAt(j) == '.')) {
         j++;
      }

      if (j < n && (expr.charAt(j) == 'e' || expr.charAt(j) == 'E')) {
         int k = j + 1;
         if (k < n && (expr.charAt(k) == '+' || expr.charAt(k) == '-')) {
            k++;
         }

         int exponentStart = k;

         while (k < n && Character.isDigit(expr.charAt(k))) {
            k++;
         }

         if (k > exponentStart) {
            j = k;
         }
      }

      return j;
   }

   private static BigDecimal parseNumber(String numStr) throws NumberFormatException {
      try {
         if (numStr.endsWith(".")) {
            numStr = numStr + "0";
         }

         if (numStr.startsWith(".") && numStr.length() > 1) {
            numStr = "0" + numStr;
         }

         return new BigDecimal(numStr);
      } catch (NumberFormatException var2) {
         throw new NumberFormatException("Invalid number: " + numStr);
      }
   }

   private static void calculateTop(Deque<BigDecimal> numStack, Deque<String> opStack) throws NumberFormatException, ArithmeticException {
      String op = opStack.pop();
      if (!op.equals("~") && !op.equals("!")) {
         if (numStack.size() < 2) {
            throw new NumberFormatException("Invalid expression");
         } else {
            BigDecimal b = numStack.pop();
            BigDecimal a = numStack.pop();

            numStack.push(switch (op) {
               case "+" -> a.add(b);
               case "-" -> a.subtract(b);
               case "*" -> a.multiply(b);
               case "**" -> a.pow(b.intValue());
               case "/" -> {
                  if (b.compareTo(BigDecimal.ZERO) == 0) {
                     throw new ArithmeticException("Divided by 0");
                  }

                  yield a.divide(b, 10, RoundingMode.HALF_UP);
               }
               case "&" -> {
                  long aLongAnd = a.longValue();
                  long bLongAnd = b.longValue();
                  yield new BigDecimal(aLongAnd & bLongAnd);
               }
               case "|" -> {
                  long aLongOr = a.longValue();
                  long bLongOr = b.longValue();
                  yield new BigDecimal(aLongOr | bLongOr);
               }
               case "^" -> {
                  long aLongXor = a.longValue();
                  long bLongXor = b.longValue();
                  yield new BigDecimal(aLongXor ^ bLongXor);
               }
               case "<<" -> {
                  long aLongLsh = a.longValue();
                  long bLongLsh = b.longValue();
                  yield new BigDecimal(aLongLsh << (int)bLongLsh);
               }
               case ">>" -> {
                  long aLongRsh = a.longValue();
                  long bLongRsh = b.longValue();
                  yield new BigDecimal(aLongRsh >> (int)bLongRsh);
               }
               default -> throw new NumberFormatException("Invalid symbol: " + op);
            });
         }
      } else if (numStack.isEmpty()) {
         throw new NumberFormatException("Invalid expression");
      } else {
         BigDecimal a = numStack.pop();
         long aLong = a.longValue();

         long resultLong = switch (op) {
            case "~" -> ~aLong;
            case "!" -> aLong == 0L ? 1L : 0L;
            default -> throw new NumberFormatException("Invalid symbol: " + op);
         };
         numStack.push(new BigDecimal(resultLong));
      }
   }

   private static int getPriority(String op) {
      return PRIORITY.getOrDefault(op, 0);
   }

   private static boolean isValidParentheses(String expr) {
      Deque<Character> stack = new ArrayDeque<>();

      for (char c : expr.toCharArray()) {
         if (c == '(') {
            stack.push(c);
         } else if (c == ')' && (stack.isEmpty() || stack.pop() != '(')) {
            return false;
         }
      }

      return stack.isEmpty();
   }

   private static boolean isOperator(String op) {
      return PRIORITY.containsKey(op);
   }

   static {
      PRIORITY.put("~", 4);
      PRIORITY.put("!", 4);
      PRIORITY.put("&", 3);
      PRIORITY.put("^", 3);
      PRIORITY.put("|", 3);
      PRIORITY.put("<<", 3);
      PRIORITY.put(">>", 3);
      PRIORITY.put("**", 3);
      PRIORITY.put("*", 2);
      PRIORITY.put("/", 2);
      PRIORITY.put("+", 1);
      PRIORITY.put("-", 1);
      PRIORITY.put("(", 0);
   }
}
