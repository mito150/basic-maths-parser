import java.util.Vector;

public class EvalExpression {

    private final static String Err = "NaN";

    // TODO: cut the decimals of the result: 2.045125 vs 2.0451250000000005

    /**
     * String -> String
     * Checks the basic arithmetic expression and produces the result of the calculation
     *
     * @param exp basic arithmetic String to be parsed
     * @return the calculated basic arithmetic expression as String
     */
    public String ParseBasicExpression(String exp) {
        Vector<String> parsed_exp = new Vector<String>();
        Vector<String> simplified_exp;
        String[] exploded_exp;
        String t1;
        Boolean lastT1isNumber = false;

        // Split the expression
        exploded_exp = exp.split(" ");

        for(int i = 0; i < exploded_exp.length; i++) {
            t1 = exploded_exp[i];

            // if we found some extra spaces, do next loop
            if(t1.equals("")) continue;

            // Examine if the expression is well formed: 5+4*10-1 vs 5***3-+4
            // if we are in numbers position: 5 + 3 -> [0]:5, [1]:+, [2]:3
            if((i % 2) == 0) {
                if(!t1.equals("*") && !t1.equals("/") && !t1.equals("+") && !t1.equals("-")) {
                    // If there are more than 1 point... : 2.2, 2.2.2 or 2...2
                    if((t1.split("\\.").length > 2) || (t1.matches("\\.+"))) {
                        return Err;
                    }
                    // Fix for NaN: NaN9 - 8
                    else if(t1.contains("NaN")) {
                        return Err;
                    }
                    // Fix for malformed numbers like 5.E, 5E.4E, ...
                    else if(!isNumeric(t1)) return Err;

                    lastT1isNumber = true;
                    parsed_exp.add(t1);
                } else return Err;
            }

            // if we are in operator position, we want an operator
            else if(t1.equals("*") || t1.equals("/") || t1.equals("+") || t1.equals("-")) {
                // Return Error if last t1 is not a number: 5**2
                if(!lastT1isNumber) return Err;

                lastT1isNumber = false;
                parsed_exp.add(t1);
            } else return Err;
        }

        // Simplify Expression (change x*x or x/x to calculated values)
        simplified_exp = SimplifyExpression(parsed_exp);

        return CalculateExpression(simplified_exp);
    }


    /**
     * Vector<String> -> Vector<String>
     * At this point we have a parsed Vector<String> expression
     * This method produces the results of the operations *, / and %
     *
     * @param exp Arithmetic Vector<String> expression to simplify
     * @return a arithmetically simplified Vector<String> expression
     */
    private Vector<String> SimplifyExpression(Vector<String> exp) {
        Vector<String> result = new Vector<String>();
        double tmp;

        // If we have only a number, return that number
        // But check if it's not a null Vector like NaN
        if(exp.size() > 0 && exp.size() < 3) {
            // If it contains a percentage return it as number. Ex: 50% -> 0.5
            if(isPercentage(exp.get(0))) { //
                // Remove % from the String and convert it to the real (plain and operable) number
                String spercent = exp.get(0).replace("%", "");
                double npercent = Double.parseDouble(spercent) / 100;

                result.add(String.valueOf(npercent));
            }
            // If not, add and return the same
            else result.add(exp.get(0));

            return result;
        }

        // Loop Vector and protect for out of bounds
        for(int i = 0; i <= exp.size(); i++) {

            // If we are in operator position...
            if(i % 2 == 1) {
                // If we have at least 3 items "x * x" and don't go out of bounds...
                if(i + 1 < exp.size()) {
                    if(exp.get(i).equals("*")) {

                        // If it appears in the first group...
                        if(i == 1) {
                            // Check if the first operand is a percentage and simplify it by: exp[0] / 100
                            if(isPercentage(exp.get(0))) {
                                // Delete the % symbol and convert to numeric
                                tmp = Double.parseDouble(exp.get(0).replace("%", "")) / 100;
                                // Replace array value
                                exp.setElementAt(String.valueOf(tmp), 0);
                            }
                            // Both are "If" to fix a expression like 10% * 15%
                            // Check if the second operand is a percentage and simplify it by: exp[0] * (exp[2] / 100)
                            if(isPercentage(exp.get(2))) {
                                // Delete the % symbol and convert to numeric
                                tmp = Double.parseDouble(exp.get(2).replace("%", "")) / 100;
                                // Replace array value
                                exp.setElementAt(String.valueOf(tmp), 2);
                            }

                            // Calculate percentage simplified expression: exp[0] * exp[2]
                            tmp = Double.parseDouble(exp.get(0)) * Double.parseDouble(exp.get(2));
                            result.add(String.valueOf(tmp));
                        } else {
                            // Check if the right operand is percentage
                            if(isPercentage(exp.get(i + 1))) {
                                // Delete the % symbol and convert to numeric
                                tmp = Double.parseDouble(exp.get(i + 1).replace("%", "")) / 100;
                                // Replace array value
                                exp.setElementAt(String.valueOf(tmp), i + 1);
                            }

                            // Calc last item on result vector * exp[i+1]: result[size-1] * exp[i+1]
                            tmp = Double.parseDouble(result.get(result.size() - 1)) * Double.parseDouble(exp.get(i + 1));
                            result.setElementAt(String.valueOf(tmp), result.size() - 1);
                        }
                    } else if(exp.get(i).equals("/")) {
                        // If it appears in the first group...
                        if(i == 1) {
                            // Check if the first operand is a percentage and simplify it by: exp[0] / 100
                            if(isPercentage(exp.get(0))) {
                                // Delete the % symbol and convert to numeric
                                tmp = Double.parseDouble(exp.get(0).replace("%", "")) / 100;
                                // Replace array value
                                exp.setElementAt(String.valueOf(tmp), 0);
                            }
                            // Both are "If" to fix a expression like 10% / 15%
                            // Check if the second operand is a percentage and simplify it by: exp[0] / (exp[2] / 100)
                            if(isPercentage(exp.get(2))) {
                                tmp = Double.parseDouble(exp.get(2).replace("%", "")) / 100;
                                // Replace array value
                                exp.setElementAt(String.valueOf(tmp), 2);
                            }

                            // Calculate percentage simplified expression: exp[0] / exp[2]
                            tmp = Double.parseDouble(exp.get(0)) / Double.parseDouble(exp.get(2));
                            result.add(String.valueOf(tmp));
                        } else {
                            // Check if the right operand is percentage
                            if(isPercentage(exp.get(i + 1))) {
                                // Delete the % symbol and convert to numeric
                                tmp = Double.parseDouble(exp.get(i + 1).replace("%", "")) / 100;
                                // Replace array value
                                exp.setElementAt(String.valueOf(tmp), i + 1);
                            }

                            // Calc last item on result vector / exp[i+1]
                            tmp = Double.parseDouble(result.get(result.size() - 1)) / Double.parseDouble(exp.get(i + 1));
                            result.setElementAt(String.valueOf(tmp), result.size() - 1);
                        }
                    }

                    // It means the operator is + or -
                    else {
                        // If it appears in the first group...
                        if(i == 1) {
                            // Check if the first operand is a percentage and simplify it by: exp[0] / 100
                            if(isPercentage(exp.get(0))) {
                                // Delete the % symbol and convert to numeric
                                tmp = Double.parseDouble(exp.get(0).replace("%", "")) / 100;
                                // Replace array value
                                exp.setElementAt(String.valueOf(tmp), 0);
                            }
                            // Both are "If" to fix a expression like 10% / 15%
                            // Check if the second operand is a percentage and simplify it by: exp[0] * (exp[2] / 100)
                            if(isPercentage(exp.get(2))) {
                                // Add operation: "+"
                                if(exp.get(1).equals("+")) {
                                    // Calculate percentage and add it to it's parent
                                    tmp = Double.parseDouble(exp.get(0)) * (Double.parseDouble(exp.get(2).replace("%", "")) / 100);
                                    tmp += Double.parseDouble(exp.get(0));

                                    // Add the result of the operation
                                    result.add(0, String.valueOf(tmp));
                                }
                                // Subtract operation: "-"
                                else {
                                    // Calculate percentage and subtract it to it's parent
                                    tmp = Double.parseDouble(exp.get(0)) * (Double.parseDouble(exp.get(2).replace("%", "")) / 100);
                                    tmp = Double.parseDouble(exp.get(0)) - tmp;

                                    // Add the result of the operation
                                    result.add(0, String.valueOf(tmp));
                                }
                            } else {
                                // Add exp[0] exp[1]{-+} exp[2]
                                result.add(exp.get(0));
                                result.add(exp.get(1));
                                result.add(exp.get(2));
                            }
                        }
                        // 2 and next groups
                        else {
                            if(isPercentage(exp.get(i + 1))) {
                                // Add operation: "+"
                                if(exp.get(i).equals("+")) {
                                    // Calculate percentage and add it to it's parent
                                    tmp = Double.parseDouble(result.get(result.size() - 1)) * (Double.parseDouble(exp.get(i + 1).replace("%", "")) / 100);
                                    tmp += Double.parseDouble(result.get(result.size() - 1));

                                    result.setElementAt(String.valueOf(tmp), result.size() - 1);
                                }
                                // Subtract operation: "-"
                                else {
                                    // Calculate percentage and subtract it to it's parent
                                    tmp = Double.parseDouble(result.get(result.size() - 1)) * (Double.parseDouble(exp.get(i + 1).replace("%", "")) / 100);
                                    tmp = Double.parseDouble(result.get(result.size() - 1)) - tmp;

                                    result.setElementAt(String.valueOf(tmp), result.size() - 1);
                                }
                            } else {
                                // Add exp[i]{-+} exp[i+1]
                                result.add(exp.get(i));
                                result.add(exp.get(i + 1));
                            }
                        }
                    }
                }
            }
        }

        return result;
    }


    /**
     * String -> String
     * Consumes a basic arithmetic String expression with spaces between operators and deletes
     * the last inserted num or operator.
     * Ex: "5 + 6 - 5.9"
     *
     * @param exp basic arithmetic String
     * @return Err or String with the last inserted num or operator deleted.
     */
    public String deleteLast(String exp) {
        int len = exp.length();

        if(len > 0) {
            if(exp.endsWith(" ")) {
                return exp.substring(0, len - 3);
            } else return exp.substring(0, len - 1);
        } else return Err;
    }


    /**
     * String -> Boolean
     * Checks if the given String can be converted to a numeric value, which means is numeric.
     *
     * @param exp String
     * @return true if the given String is numeric
     */
    public Boolean isNumeric(String exp) {
        // Fix for percentages like 52%255
        if(exp.matches("(-?\\d+%\\d+)|(-?\\d+.\\d+%\\d+)")) return false;

        return exp.matches("(-?\\d+)|(-?\\d+.\\d+)|(-?\\d+E-?\\d+)|(-?\\d+.\\d+E-?\\d+)|(-?\\d+%)|(-?\\d+.\\d+%)");
    }


    /**
     * String -> Boolean
     * Checks if the given String is a percentage number. Ex: 4.5%
     *
     * @param exp String
     * @return true if the given String is a percentage
     */
    public Boolean isPercentage(String exp) {
        return exp.matches("(-?\\d+%)|(-?\\d+.\\d+%)");
    }


    /**
     * Double -> Boolean
     * Checks if the given number has decimal point value.
     * Ex: 245.54, 33.0
     *
     * @param num the number to check
     * @return true if the number has decimal value
     */
    public Boolean isDecimalValue(double num) {
        // Write the num to a String
        String tmp = String.valueOf(num);

        return isDecimalValue(tmp);
    }


    /**
     * String -> Boolean
     * Checks if the given number has a decimal point value.
     *
     * @param num String containing a number to check
     * @return true if the number has decimal value
     */
    public Boolean isDecimalValue(String num) {
        // Matches for 3.0 , 5.000
        if(num.matches("(-?\\d+.0+)|(-?\\d+.0+E-?\\d+)")) return false;
            // Matches for a number with decimal point
        else return num.matches("(-?\\d+.\\d+)|(-?\\d+.\\d+E-?\\d+)");
    }


    /**
     * Inverts the last number of the given CharSeq. expression
     * TIP: CharSequence and String are the same more or less
     *
     * @param exp basic arithmetic CharSeq. expression
     * @return the CharSeq. with the last number inverted
     * on error returns @param
     */
    public CharSequence invertSignNumber(CharSequence exp) {
        String[] tmp_str;
        double tmp;

        // Split expression 1:num, 2:oper, 3:num, ...
        tmp_str = exp.toString().split(" ");
        int l = tmp_str.length;

        // if expression is not empty...
        if(l > 0) {
            // And is a number...
            if(isNumeric(tmp_str[l - 1]) && !isPercentage(tmp_str[l - 1])) {
                // Invert number
                tmp = Double.parseDouble(tmp_str[l - 1]) * -1;

                // If it contains decimal point return it as Double, return as long otherwise
                // Replace the value on the Array
                if(tmp_str[l - 1].contains(".")) tmp_str[l - 1] = String.valueOf(tmp);
                else tmp_str[l - 1] = String.valueOf((long) tmp);

                // Concatenate array and return the inverted String
                return String.valueOf(invertConcatenateHelper(tmp_str));
            }
            // If it's not a number, maybe it can be a percentage
            else if(isPercentage(tmp_str[l - 1])) {
                // If it contains the minus sign, delete it (invert)
                if(tmp_str[l - 1].contains("-")) tmp_str[l - 1] = tmp_str[l - 1].replace("-", "");
                    // If it not contains the minus sign, add it (invert)
                else tmp_str[l - 1] = "-" + tmp_str[l - 1];

                return String.valueOf(invertConcatenateHelper(tmp_str));
            }
        }

        // If errors, return the given parameter
        return exp;
    }


    /**
     * String[] -> String
     * Helper method for invertSignNumber. Concatenates the String array and return it as a String
     *
     * @param exp String array
     * @return concatenated String
     */
    private String invertConcatenateHelper(String[] exp) {
        String concat_str = "";

        // Modified for (foreach like) for Android Studio recommendation
        for(String txt : exp) {
            // If it's an operator add surrounding spaces
            if(txt.matches("\\+?\\-?\\*?/?")) {
                concat_str += " " + txt + " ";
            } else concat_str += txt;
        }

        return String.valueOf(concat_str);
    }


    /**
     * Vector<String> -> String
     * Produces the final result of the parsed Vector<String> arithmetic expression
     *
     * @param exp parsed and Vector<String>
     * @return String containing the final value of the expression
     */
    private String CalculateExpression(Vector<String> exp) {
        double res = 0;
        String op;
        double t1, t2;
        int i = 0;

        // At this point we have a cleaned expression
        while(i < exp.size()) {
            // If size is < 3 it means it's only a number?
            if(exp.size() < 3) {
                // Check if it's numeric and return it, otherwise return Err
                if(isNumeric(exp.get(i))) {
                    // Check if the number has decimal value and return the value with or without dot
                    // Ex: 5.0 -> 5 ;; 5.1 -> 5.1
                    if(isDecimalValue(exp.get(i))) {
                        res = Double.parseDouble(exp.get(i));
                        // Fix for persistent xxx.0 values, for example with E numbers: 1.7E5
                        if(String.valueOf(res).matches("(-?\\d+.0)|(-?\\d+.0E-?\\d+)")) {
                            return String.valueOf((long) res);
                        }
                        return String.valueOf(res);
                    } else {
                        // Fix for .0, .0000, ...
                        if(exp.get(i).matches("\\.0+")) return String.valueOf("0");
                            // Fix for 5.0, -15.0, 45.000, ... and fix for 5., -4., 35., ...
                        else if(exp.get(i).matches("-?\\d+.0+") || exp.get(i).matches("-?\\d+.")) {
                            double dnum = Double.parseDouble(exp.get(i));
                            long lnum = (long) dnum;

                            return String.valueOf(lnum);
                        }
                        // Fix for long depth Ex: 5.000000000000000000001111
                        else if(exp.get(i).matches("-?\\d+.0+\\d+"))
                            return String.valueOf(Double.parseDouble(exp.get(i)));

                        return String.valueOf(Long.parseLong(exp.get(i)));
                    }
                } else return Err;
            }

            // Perform operations on the first group (5,+,2)
            else if(i == 0) {
                t1 = Double.parseDouble(exp.get(i));
                t2 = Double.parseDouble(exp.get(i + 2));
                op = exp.get(i + 1);

                // Perform operations
                if(op.equals("+")) res = t1 + t2;
                else if(op.equals("-")) res = t1 - t2;
                else if(op.equals("*")) res = t1 * t2;
                else res = t1 / t2;

                // Increment counter to match next group
                i += 3;
            } else {
                op = exp.get(i);
                t1 = Double.parseDouble(exp.get(i + 1));

                // Perform operations
                if(op.equals("+")) res += t1;
                else if(op.equals("-")) res -= t1;
                else if(op.equals("*")) res *= t1;
                else res /= t1;

                // Increment counter to match next group
                i += 2;
            }
        }

        // Check if it has decimal point, if not, return integer(long) value
        if(isDecimalValue(res)) return String.valueOf(res);
        else return String.valueOf((long) res);
    }
}
