package at.enfilo.def.client.shell.formatter;

import at.enfilo.def.common.api.IAssociable;

abstract class ShellFormatter<T> implements IAssociable<Class<? extends T>> {

    private final Class<T> formattableClass;

	protected ShellFormatter(Class<T> formattableClass) {
        this.formattableClass = formattableClass;
	}

	protected abstract String doFormat(T t, char[] shifted);

	public String format(Object o, int shift) {
	    if (o != null && o.getClass().isAssignableFrom(formattableClass)) {
	    	char[] shifted = new char[shift];
	    	for (int i = 0; i < shift; i++) {
	    		shifted[i] = ' ';
			}
			return doFormat(formattableClass.cast(o), shifted);
		}
        throw new IllegalArgumentException("Received object \"" + o + "\" cannot be formatted using this formatter.");
    }

    @Override
    public Class<? extends T> getAssociation() {
        return formattableClass;
    }
}
