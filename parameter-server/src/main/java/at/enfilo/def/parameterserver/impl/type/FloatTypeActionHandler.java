package at.enfilo.def.parameterserver.impl.type;

public class FloatTypeActionHandler implements ITypeActionHandler {
    @Override
    public String getAssociation() {
        return "float";
    }

    @Override
    public void addi(Object sum, Object summand) {
        float[] data = (float[]) sum;
        float[] data2 = (float[]) summand;
        for (int i = 0; i < data.length; i++) {
            data[i] += data2[i];
        }
    }
}
