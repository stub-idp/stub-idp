package stubidp.test.integration.steps;
import javax.ws.rs.core.Form;

public class FormBuilder {
    private final Form form;

    public static FormBuilder newForm() {
        return new FormBuilder();
    }

    private FormBuilder() {
        this.form = new Form();
    }

    public FormBuilder withParam(String name, String value) {
        this.form.param(name, value);
        return this;
    }
    public Form build() {
        return this.form;
    }
}
