package com.github.guraame.human;

import com.github.guraame.human.concept.BaseConceptType;
import com.github.guraame.human.concept.Concept;
import com.github.guraame.human.concept.ConceptType;
import com.github.guraame.human.debug.Debugger;
import com.github.guraame.human.idea.Idea;
import com.github.guraame.human.idea.IdeaManger;
import com.github.guraame.human.input.Message;
import com.github.guraame.human.parse.MessageParser;
import com.github.guraame.human.parse.Token;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public final class Human {
    public static IdeaManger ideaManger = new IdeaManger();

    public static void main(String[] args) {
        List<Point2D> edge = List.of(
                new Point2D.Double(0, 0),
                new Point2D.Double(1, 1),
                new Point2D.Double(2, 4),
                new Point2D.Double(3, 9)
        );
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        WeightedObservedPoints obs = new WeightedObservedPoints();
        edge.forEach(p -> obs.add(p.getX(), p.getY()));
        double[] coeff = fitter.fit(obs.toList());
        System.out.println("f(x) = "
                + String.format("%f", coeff[2]) + "x^2 + "
                + String.format("%f", coeff[1]) + "x + "
                + String.format("%f", coeff[0]));

        var concept = new HashMap<ConceptType, Concept<BaseConceptType>>();
        concept.put(ConceptType.NULL, new Concept<>() {
            @Override
            public BaseConceptType toSomething() {
                return BaseConceptType.NOTHING;
            }
        });
        concept.put(ConceptType.EXIST, new Concept<>() {
            @Override
            public BaseConceptType toSomething() {
                return BaseConceptType.ANY;
            }
        });
        Scanner scanner = new Scanner(System.in);
        ideaManger.addRelation(Token.of("name"), Idea.of("human"));
        Debugger.setTrain(true);
        while (true) {
            MessageParser.parseMessage(Message.of(scanner.nextLine())).printIn(System.out);
        }
    }
}
