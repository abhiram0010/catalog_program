import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Solution {
  public static void main(String[] args) {
    try {
      // Process both test cases
      BigInteger secret1 = processTestCase("test_case_1.json");
      BigInteger secret2 = processTestCase("test_case_2.json");

      // Print the secrets
      System.out.println("Secret for test case 1: " + secret1);
      System.out.println("Secret for test case 2: " + secret2);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Process a single test case
  private static BigInteger processTestCase(String filename) throws Exception {
    // Read the JSON file content
    String jsonContent = readFile(filename);

    // Extract k value using regex
    Pattern kPattern = Pattern.compile("\"k\"\\s*:\\s*(\\d+)");
    Matcher kMatcher = kPattern.matcher(jsonContent);
    if (!kMatcher.find()) {
      throw new RuntimeException("Cannot find k value in JSON");
    }
    int k = Integer.parseInt(kMatcher.group(1));

    // Extract points using regex
    List<Point> points = new ArrayList<>();

    // Match point entries like "1": { "base": "10", "value": "4" }
    Pattern pointPattern = Pattern
        .compile("\"(\\d+)\"\\s*:\\s*\\{\\s*\"base\"\\s*:\\s*\"(\\d+)\"\\s*,\\s*\"value\"\\s*:\\s*\"([^\"]+)\"");
    Matcher pointMatcher = pointPattern.matcher(jsonContent);

    while (pointMatcher.find()) {
      int x = Integer.parseInt(pointMatcher.group(1));
      int base = Integer.parseInt(pointMatcher.group(2));
      String encodedValue = pointMatcher.group(3);

      BigInteger y = convertFromBase(encodedValue, base);
      points.add(new Point(BigInteger.valueOf(x), y));
    }

    // We need at least k points for interpolation
    if (points.size() < k) {
      throw new RuntimeException("Not enough points provided for interpolation");
    }

    // Use first k points for interpolation
    List<Point> selectedPoints = points.subList(0, k);

    // Find the constant term of the polynomial using Lagrange interpolation
    return lagrangeInterpolation(selectedPoints, BigInteger.ZERO);
  }

  // Read file content as string
  private static String readFile(String filename) throws Exception {
    StringBuilder content = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line);
      }
    }
    return content.toString();
  }

  // Convert a string from given base to a BigInteger
  private static BigInteger convertFromBase(String value, int base) {
    BigInteger result = BigInteger.ZERO;
    String digits = "0123456789abcdefghijklmnopqrstuvwxyz";

    for (int i = 0; i < value.length(); i++) {
      char c = Character.toLowerCase(value.charAt(i));
      int digit = digits.indexOf(c);

      if (digit >= base || digit == -1) {
        throw new IllegalArgumentException("Invalid digit '" + c + "' for base " + base);
      }

      result = result.multiply(BigInteger.valueOf(base)).add(BigInteger.valueOf(digit));
    }

    return result;
  }

  // Lagrange interpolation to find f(at) - in our case, f(0) which is the
  // constant term
  private static BigInteger lagrangeInterpolation(List<Point> points, BigInteger at) {
    BigInteger result = BigInteger.ZERO;

    for (int i = 0; i < points.size(); i++) {
      Point point = points.get(i);
      BigInteger term = point.y;

      for (int j = 0; j < points.size(); j++) {
        if (i == j)
          continue;

        Point other = points.get(j);

        // Calculate (at - x_j) / (x_i - x_j)
        BigInteger numerator = at.subtract(other.x);
        BigInteger denominator = point.x.subtract(other.x);

        term = term.multiply(numerator).divide(denominator);
      }

      result = result.add(term);
    }

    return result;
  }

  // Point class to store x and y coordinates
  private static class Point {
    BigInteger x;
    BigInteger y;

    Point(BigInteger x, BigInteger y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public String toString() {
      return "(" + x + ", " + y + ")";
    }
  }
}