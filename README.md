Sebastian_Raczyło_Java_Krakow
# 1. How to run:
```
java -jar target/payment-optimizer-1.0-SNAPSHOT-jar-with-dependencies.jar /absolute/path/to/orders.json /absolute/path/to/paymentMethods.json
```
- First argument: Absolute path to the orders.json file
- Second argument: Absolute path to the paymentMethods.json file

# 2. How it works:

## App.java:
- Parses both JSON files into Java objects (Order, Promotion)
- Calls optimizePayments() method from PaymentService to perform optimization

## PaymentService.java:
Orders are sorted in descending order of value to prioritize applying the best discounts to the most expensive orders.

For each order, the algorithm attempts the following, in order:
- Apply the best available promotion (from the order’s allowed promotions)
- Pay the full amount using points (if possible)
- Pay partially using points (if not enough points available)
- Pay the remaining amount using a single card, without applying card discount

When paying partially with points, the remaining value must be paid using a single card. In such a case, the algorithm chooses the card with sufficient limit and the worst discount — since the card discount does not apply in this scenario.

# 3. Tools:
- Maven to generate project structure and manage libraries
- Maven Assembly plugin for generating fat-jar that can be found in target directory.
- Junit for 6 basic tests that covers all functions from PaymentService.
- Jackson to parse Json files.
