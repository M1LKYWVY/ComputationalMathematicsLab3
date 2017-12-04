package sample;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MainController {

    @FXML
    public CheckBox pointsButton;
    @FXML
    public CheckBox functionButton;
    @FXML
    public CheckBox splineButton;
    ArrayList<XYChart.Series<Number, Number>>  functionsList = new ArrayList<>();
    ArrayList<Double> y = new ArrayList<>();
    ArrayList<Double> x = new ArrayList<>();
    ArrayList<Double> bList = new ArrayList<>(); //for diagonal matrix (side)
    ArrayList<Double> BList = new ArrayList<>(); //for diagonal matrix
    ArrayList<Double> aList = new ArrayList<>(); //For diagonal matrix (side)
    ArrayList<Double> AList = new ArrayList<>(); //for diagonal matrix
    ArrayList<Double> FList = new ArrayList<>(); //for diagonal matrix
    @FXML
    public LineChart<Number, Number> numberLineChart;
    @FXML
    public Button getCoefficients;
    @FXML
    public TextField from;
    @FXML
    public TextField to;
    @FXML
    public TextField amount;
    @FXML
    public TextField function;
    @FXML
    public TextField amountNumber;
    @FXML
    public Label labelOfAmount;
    String currentGraphic = "";
    String currentFrom = "";
    String currentTo = "";
    String currentAmount = "";
    boolean isBuilt = false;

    public void makeGraphic(ActionEvent actionEvent) throws Exception {
        try {
            ArrayList<Double> xList = new ArrayList<>();
            ArrayList<Double> yList = new ArrayList<>();
            if (amount.getText().equals("") ||
                    from.getText().equals("") ||
                    function.getText().equals("") ||
                    to.getText().equals(""))
                return;
            if (Integer.parseInt(amount.getText()) > 150 ||
                    Double.parseDouble(from.getText()) > Double.parseDouble(to.getText())) {
                return;
            }
            double step = Math.abs(Double.parseDouble(to.getText()) - Double.parseDouble(from.getText())) / Integer.parseInt(amount.getText());
            if (currentGraphic.equals(function.getText())
                    && currentFrom.equals(from.getText())
                    && currentTo.equals(to.getText())
                    && currentAmount.equals(amount.getText())) {
                return;
            } else {
                currentGraphic = function.getText();
                currentFrom = from.getText();
                currentTo = to.getText();
                currentAmount = amount.getText();
                if (isBuilt) {
                    if (numberLineChart.getData().size()==1) {
                        numberLineChart.getData().remove(0);
                    }
                    else if ((numberLineChart.getData().size()==2)) {
                        numberLineChart.getData().remove(1);
                        numberLineChart.getData().remove(0);
                    }

                }
            }
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Function");
            Expression ex = new ExpressionBuilder(function.getText())
                    .variables("x").build();
            for (double d = Double.parseDouble(from.getText()); d <= Double.parseDouble(to.getText()); d += step) {
                ex.setVariable("x", d);
                ValidationResult res = ex.validate();
                if (!res.isValid()) {
                    for (String err : res.getErrors()) {
                        System.out.println(err);
                    }
                }
                double evaluation = ex.evaluate();
                if (Double.isNaN(evaluation) || Double.isInfinite(evaluation)) continue;
                yList.add(evaluation);
                xList.add(d);
                series.getData().add(new XYChart.Data<>(d, ex.evaluate()));
            }
            double x[], y[], h[], l[], delta[], lambda[], c[], d[], b[];

            int N = xList.size();

            x = new double[N];
            y = new double[N];
            h = new double[N];
            l = new double[N];
            delta = new double[N];
            lambda = new double[N];
            c = new double[N];
            d = new double[N];
            b = new double[N];

            for (int i = 0; i < N; ++i) {
                x[i] = xList.get(i);
                y[i] = yList.get(i);
            }

            for (int k = 1; k < N; k++) {
                h[k] = x[k] - x[k - 1];
                if (h[k] == 0) {
                    System.out.printf("\nError, x[%d]=x[%d]\n", k, k - 1);
                    return;
                }
                l[k] = (y[k] - y[k - 1]) / h[k];
            }

            delta[1] = -h[2] / (2 * (h[1] + h[2]));
            lambda[1] = 1.5 * (l[2] - l[1]) / (h[1] + h[2]);
            for (int k = 3; k < N; k++) {
                delta[k - 1] = -h[k] / (2 * h[k - 1] + 2 * h[k] + h[k - 1] * delta[k - 2]);
                lambda[k - 1] = (3 * l[k] - 3 * l[k - 1] - h[k - 1] * lambda[k - 2]) /
                        (2 * h[k - 1] + 2 * h[k] + h[k - 1] * delta[k - 2]);
            }

            c[0] = 0;
            c[N - 1] = 0;
            for (int k = N - 1; k >= 2; k--) {
                c[k - 1] = delta[k - 1] * c[k] + lambda[k - 1];
            }
            for (int k = 1; k < N; k++) {
                d[k] = (c[k] - c[k - 1]) / (3 * h[k]);
                b[k] = l[k] + (2 * c[k] * h[k] + h[k] * c[k - 1]) / 3;
            }
            ArrayList<String> splineFunctions = new ArrayList<>();
            for (int i = 1; i < N; ++i) {
                String bSign, cSign, dSign;
                bSign = "";
                cSign = "";
                dSign = "";
                if (b[i] >= 0) {
                    bSign = "+";
                }
                if (c[i] >= 0) {
                    cSign = "+";
                }

                if (d[i] >= 0) {
                    dSign = "+";
                }

                splineFunctions.add(y[i] + bSign + b[i] + "*" + "(x-" + x[i] + ")" + cSign + c[i] + "*" + "(x-" + x[i] + ")^2" + dSign + d[i] + "*" + "(x-" + x[i] + ")^3");
            }
            System.out.printf("\nf(x)=" + currentGraphic +
                    "\na[i]\tb[i]\tc[i]\td[i]\n");
            for (int k = 1; k < N; k++) {
                System.out.printf("%f\t%f\t%f\t%f\n", y[k], b[k], c[k], d[k]);
            }
            XYChart.Series<Number, Number> splineSeries = new XYChart.Series<>();
            splineSeries.setName("Spline function");
            step = step / 2;
            for (String function: splineFunctions) {
                System.out.println("\nf(x): " + function);
            }
            System.out.println("\n\n");
            for (int i = 0; i < splineFunctions.size(); ++i) {
                ex = new ExpressionBuilder(splineFunctions.get(i))
                        .variables("x").build();
                for (double j = xList.get(i); j <= xList.get(i + 1); j += step) {
                    ex.setVariable("x", j);
                    ValidationResult res = ex.validate();
                    if (!res.isValid()) {
                        for (String err : res.getErrors()) {
                            System.out.println(err);
                        }
                    }
                    double evaluation = ex.evaluate();
                    if (Double.isNaN(evaluation) || Double.isInfinite(evaluation)) continue;
                    splineSeries.getData().add(new XYChart.Data<>(j, ex.evaluate()));
                    if (amount.getText().equals("3") &&
                            i == splineFunctions.size()-1) {
                        ex.setVariable("x", j+step);
                        res = ex.validate();
                        if (!res.isValid()) {
                            for (String err : res.getErrors()) {
                                System.out.println(err);
                            }
                        }
                        evaluation = ex.evaluate();
                        if (Double.isNaN(evaluation) || Double.isInfinite(evaluation)) continue;
                        splineSeries.getData().add(new XYChart.Data<>(j+step, ex.evaluate()));
                    }
                }
            }
            pointsButton.setSelected(true);
            functionButton.setSelected(true);
            splineButton.setSelected(true);
            numberLineChart.getData().add(series);
            numberLineChart.getData().add(splineSeries);
            functionsList = new ArrayList<>(numberLineChart.getData());
            isBuilt = true;

//            pointsButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
//                if (newValue) {
//                    numberLineChart.setCreateSymbols(true);
//                }
//                else {
//                    numberLineChart.setCreateSymbols(false);
//                }
//            });
//            functionButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
//                System.out.println(functionsList.size());
//                System.out.println(1);
//                int index = 0;
//                for (XYChart.Series<Number, Number> series1: functionsList) {
//                    if (series1.getName().equals("Function")) {
//                    index = functionsList.indexOf(series1);
//                    }
//                }
//
//                if (newValue) {
//                   numberLineChart.getData().add(functionsList.get(index));
//                }
//                else {
//                    for (XYChart.Series<Number, Number> series1: numberLineChart.getData()) {
//                        if (series1.getName().equals("Function")) {
//                            index = numberLineChart.getData().indexOf(series1);
//                        }
//                    }
//                    numberLineChart.getData().remove(index);
//                    return;
//                }
//            });
//            splineButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
//                int index = 0;
//                for (XYChart.Series<Number, Number> series1: functionsList) {
//                    if (series1.getName().equals("Spline function")) {
//                        index = functionsList.indexOf(series1);
//                    }
//                }
//
//                if (newValue) {
//                    numberLineChart.getData().add(functionsList.get(index));
//                }
//                else {
//                    for (XYChart.Series<Number, Number> series1: numberLineChart.getData()) {
//                        if (series1.getName().equals("Spline function")) {
//                            index = numberLineChart.getData().indexOf(series1);
//                        }
//                    }
//                    numberLineChart.getData().remove(index);
//                }
//            });



        }
        catch (ArithmeticException | ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }



    }

    public void wait(ActionEvent actionEvent) throws InterruptedException {
        Thread.sleep(600);
    }

    public void modifyFunc(ActionEvent event) throws InterruptedException {
        if (!isBuilt) {
            return;
        }
        if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            boolean newValue = chk.isSelected();
            switch (chk.getText()) {
                case "Points": {
                    if (newValue) {
                        numberLineChart.setCreateSymbols(true);
                    }
                    else {
                        numberLineChart.setCreateSymbols(false);
                    }
                    break;
                }
                case "Function": {
                    int index = 0;
                    for (XYChart.Series<Number, Number> series1: functionsList) {
                        if (series1.getName().equals("Function")) {
                            index = functionsList.indexOf(series1);
                        }
                    }

                    if (newValue) {
                        numberLineChart.getData().add(functionsList.get(index));
                    }
                    else {
                        for (XYChart.Series<Number, Number> series1: numberLineChart.getData()) {
                            if (series1.getName().equals("Function")) {
                                index = numberLineChart.getData().indexOf(series1);
                            }
                        }
                        numberLineChart.getData().remove(index);
                    }

                    break;
                }
                case "Spline Functions": {
                    int index = 0;
                    for (XYChart.Series<Number, Number> series1: functionsList) {
                        if (series1.getName().equals("Spline function")) {
                            index = functionsList.indexOf(series1);
                        }
                    }

                    if (newValue) {
                        numberLineChart.getData().add(functionsList.get(index));
                    }
                    else {
                        for (XYChart.Series<Number, Number> series1: numberLineChart.getData()) {
                            if (series1.getName().equals("Spline function")) {
                                index = numberLineChart.getData().indexOf(series1);
                            }
                        }
                        numberLineChart.getData().remove(index);
                    }
                    break;
                }
            }
        }
        Thread.sleep(500);
    }

//    public void getCoefficients(ActionEvent actionEvent) {
//        try{
//            Stage stage = new Stage();
//            stage.initModality(Modality.APPLICATION_MODAL);
//            stage.setTitle("ABC");
//            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("coefficient.fxml")));
//            stage.setScene(scene);
//            stage.show();
//
//        }
//        catch (IOException ignored) {
//
//        }
//    }

//    public double getH(int index) {
//        return xList.get(index+1)-xList.get(index);
//    }

//    public double getF(int index) {
//        Expression ex = new ExpressionBuilder(function.getText())
//                .variables("x").build();
//        ex.setVariable("x", xList.get(index));
//        ValidationResult res = ex.validate();
//        if (!res.isValid()) {
//            for (String err : res.getErrors()) {
//                System.out.println(err);
//            }
//        }
//        double evaluation = ex.evaluate();
//        if (Double.isNaN(evaluation) || Double.isInfinite(evaluation)) {
//            throw new ArithmeticException();
//        }
//        return evaluation;
//    }

//    public void updateLists(ArrayList<Double> xList, ArrayList<Double> yList) {
//       this.y = yList;
//       this.x = xList;
//    }
}
