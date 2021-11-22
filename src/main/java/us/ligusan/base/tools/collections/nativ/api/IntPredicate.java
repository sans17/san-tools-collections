package us.ligusan.base.tools.collections.nativ.api;

public interface IntPredicate
{
    boolean test(int intToTest);
    
    default IntPredicate and(IntPredicate other) {
        return i -> test(i) && other.test(i);
    }
    
    default IntPredicate negate() {
        return i -> !test(i);
    }
    
    default IntPredicate or(IntPredicate other) {
        return i -> test(i) || other.test(i);
    }
}
