package com.readboy.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author oubin
 * @date 2018/12/30
 */
public class ListUtils {

    private ListUtils() {
    }

    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T t : list) {
            if (predicate.test(t)) {
                result.add(t);
            }
        }
        return result;
    }

    public static abstract class Predicate<T> {

        /**
         * Evaluates this predicate on the given argument.
         *
         * @param t the input argument
         * @return {@code true} if the input argument matches the predicate,
         * otherwise {@code false}
         */
        abstract boolean test(T t);

        /**
         * Returns a composed predicate that represents a short-circuiting logical
         * AND of this predicate and another.  When evaluating the composed
         * predicate, if this predicate is {@code false}, then the {@code other}
         * predicate is not evaluated.
         *
         * <p>Any exceptions thrown during evaluation of either predicate are relayed
         * to the caller; if evaluation of this predicate throws an exception, the
         * {@code other} predicate will not be evaluated.
         *
         * @param other a predicate that will be logically-ANDed with this
         *              predicate
         * @return a composed predicate that represents the short-circuiting logical
         * AND of this predicate and the {@code other} predicate
         * @throws NullPointerException if other is null
         */
        public Predicate<T> and(Predicate<? super T> other) {
            Objects.requireNonNull(other);
            return new Predicate<T>() {
                @Override
                public boolean test(T t) {
                    return Predicate.this.test(t) && other.test(t);
                }
            };
        }

        /**
         * Returns a predicate that represents the logical negation of this
         * predicate.
         *
         * @return a predicate that represents the logical negation of this
         * predicate
         */
        public Predicate<T> negate() {
            return new Predicate<T>() {
                @Override
                public boolean test(T t) {
                    return !Predicate.this.test(t);
                }
            };
        }

        /**
         * Returns a composed predicate that represents a short-circuiting logical
         * OR of this predicate and another.  When evaluating the composed
         * predicate, if this predicate is {@code true}, then the {@code other}
         * predicate is not evaluated.
         *
         * <p>Any exceptions thrown during evaluation of either predicate are relayed
         * to the caller; if evaluation of this predicate throws an exception, the
         * {@code other} predicate will not be evaluated.
         *
         * @param other a predicate that will be logically-ORed with this
         *              predicate
         * @return a composed predicate that represents the short-circuiting logical
         * OR of this predicate and the {@code other} predicate
         * @throws NullPointerException if other is null
         */
        public Predicate<T> or(Predicate<? super T> other) {
            Objects.requireNonNull(other);
            return new Predicate<T>() {
                @Override
                public boolean test(T t) {
                    return Predicate.this.test(t) || other.test(t);
                }
            };
        }

        /**
         * Returns a predicate that tests if two arguments are equal according
         * to {@link Objects#equals(Object, Object)}.
         *
         * @param <T>       the type of arguments to the predicate
         * @param targetRef the object reference with which to compare for equality,
         *                  which may be {@code null}
         * @return a predicate that tests if two arguments are equal according
         * to {@link Objects#equals(Object, Object)}
         */
        public static <T> Predicate<T> isEqual(Object targetRef) {
            return (null == targetRef)
                    ? null
                    : new Predicate<T>() {
                @Override
                boolean test(T t) {
                    return targetRef.equals(t);
                }
            };

        }
    }
}
