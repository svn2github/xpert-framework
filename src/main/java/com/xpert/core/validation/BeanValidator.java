package com.xpert.core.validation;

import com.xpert.core.conversion.NumberUtils;
import com.xpert.i18n.XpertResourceBundle;
import com.xpert.i18n.I18N;
import com.xpert.utils.StringUtils;
import com.xpert.faces.utils.ValueExpressionAnalyzer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ValueExpression;
import javax.el.ValueReference;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.validation.constraints.*;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

/**
 *
 * @author Ayslan
 */
public class BeanValidator extends javax.faces.validator.BeanValidator {

    private static final Logger logger = Logger.getLogger(BeanValidator.class.getName());
    private static final List<Class> VALIDATION_TYPES = new ArrayList<Class>();

    static {
        VALIDATION_TYPES.add(NotNull.class);
        VALIDATION_TYPES.add(NotBlank.class);
        VALIDATION_TYPES.add(NotEmpty.class);
        VALIDATION_TYPES.add(Max.class);
        VALIDATION_TYPES.add(Min.class);
        VALIDATION_TYPES.add(Size.class);
        VALIDATION_TYPES.add(Range.class);
        VALIDATION_TYPES.add(Email.class);
        VALIDATION_TYPES.add(DecimalMax.class);
        VALIDATION_TYPES.add(DecimalMin.class);
        VALIDATION_TYPES.add(URL.class);
        VALIDATION_TYPES.add(Past.class);
        VALIDATION_TYPES.add(Future.class);
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {
        try {
            super.validate(context, component, value);
        } catch (ValidatorException ex) {

            ValueReference valueReference = getValueReference(context, component);
            List<FacesMessage> messages = new ArrayList<FacesMessage>();

            if (ex.getFacesMessages() == null || !ex.getFacesMessages().contains(ex.getFacesMessage())) {
                messages.add(ex.getFacesMessage());
            }

            if (ex.getFacesMessages() != null) {
                messages.addAll(ex.getFacesMessages());
            }

            for (FacesMessage facesMessage : messages) {
                String message = getMessage(facesMessage.getSummary(), valueReference);
                facesMessage.setSummary(message);
                facesMessage.setDetail(message);
            }

            throw new ValidatorException(messages);
        }

    }

    public ValueReference getValueReference(FacesContext context, UIComponent component) {

        ValueExpression valueExpression = component.getValueExpression("value");
        ValueExpressionAnalyzer expressionAnalyzer = new ValueExpressionAnalyzer(valueExpression);

        return expressionAnalyzer.getReference(context.getELContext());
    }

    /**
     * The messages are formated like:
     *
     * {org.hibernate.validator.constraints.NotBlank.message}
     * {javax.validation.constraints.NotNull.message}
     *
     *
     * @param message
     * @return
     */
    private Class getViolation(String message) {
        for (Class clazz : VALIDATION_TYPES) {
            if (message.contains(clazz.getName())) {
                return clazz;
            }
        }
        return null;
    }

    public String getMessage(String message, ValueReference valueReference) {
        Class violation = getViolation(message);
        if (violation != null) {
            String object = getAttributeName(valueReference, valueReference.getBase().getClass());

            if (violation.equals(Email.class)) {
                return XpertResourceBundle.get("invalidEmail");
            }

            if (violation.equals(URL.class)) {
                return XpertResourceBundle.get("invalidURL");
            }

            if (violation.equals(NotNull.class) || violation.equals(NotEmpty.class) || violation.equals(NotBlank.class)) {
                return object + " " + XpertResourceBundle.get("isRequired");
            }
            if (violation.equals(Past.class)) {
                return object + " " + XpertResourceBundle.get("mustBeAPastDate");
            }
            if (violation.equals(Future.class)) {
                return object + " " + XpertResourceBundle.get("mustBeAFutureDate");
            }

            return getMessageWithDefinedValue(object, valueReference, violation);

        }
        return message.replace("{", "").replace("}", "");
    }

    /**
     * return class from resourcebundle the message for: simple name (First
     * Letter lowercase) + "." + property
     *
     * Ex: Class Person and attribute name - person.name
     *
     * @return
     */
    public String getAttributeName(ValueReference valueReference, Class clazz) {
        return I18N.getAttributeName(clazz,  valueReference.getProperty().toString());
    }

    /**
     *
     * @Size id for String or Collections/Map, the message is diferent from the
     * two cases
     * @Max and
     * @Min is for Numbers (Long, BigInteger, etc...)
     *
     * @param object
     * @param valueReference
     * @param violation
     * @return
     */
    public String getMessageWithDefinedValue(String object, ValueReference valueReference, Class violation) {

        AnnotationFromViolation annotationFromViolation = getAnnotation(valueReference, violation);
        Annotation annotation = annotationFromViolation.getAnnotation();

        if (violation.equals(Max.class)) {
            Long max = ((Max) annotation).value();
            return object + " " + XpertResourceBundle.get("numberMax", max);
        }
        if (violation.equals(Min.class)) {
            Long min = ((Min) annotation).value();
            return object + " " + XpertResourceBundle.get("numberMin", min);
        }
        if (violation.equals(DecimalMax.class)) {
            String max = ((DecimalMax) annotation).value();
            return object + " " + XpertResourceBundle.get("numberMax", NumberUtils.convertToNumber(max));
        }
        if (violation.equals(DecimalMin.class)) {
            String min = ((DecimalMin) annotation).value();
            return object + " " + XpertResourceBundle.get("numberMin", NumberUtils.convertToNumber(min));
        }

        if (violation.equals(Size.class)) {

            Integer min = ((Size) annotation).min();
            Integer max = ((Size) annotation).max();

            if (min != null && max != null && min > 0 && max < Integer.MAX_VALUE) {
                if (annotationFromViolation.isForChar()) {
                    return object + " " + XpertResourceBundle.get("charMaxMin", min, max);
                } else {
                    return object + " " + XpertResourceBundle.get("sizeMaxMin", max);
                }
            }
            if (min != null && min > 0) {
                if (annotationFromViolation.isForChar()) {
                    return object + " " + XpertResourceBundle.get("charMin", min);
                } else {
                    return object + " " + XpertResourceBundle.get("sizeMin", min);
                }
            }
            if (max != null && max > 0) {
                if (annotationFromViolation.isForChar()) {
                    return object + " " + XpertResourceBundle.get("charMax", max);
                } else {
                    return object + " " + XpertResourceBundle.get("sizeMax", max);
                }
            }
        }

        return "";
    }

    public AnnotationFromViolation getAnnotation(ValueReference valueReference, Class violation) {
        try {

            Field field = getDeclaredField(valueReference.getBase().getClass(), valueReference.getProperty().toString());

            if (field != null) {
                return new AnnotationFromViolation(field.getAnnotation(violation), isChar(field));
            }

            Method method = getDeclaredMethod(valueReference.getBase().getClass(), valueReference.getProperty().toString());
            if (method != null) {
                return new AnnotationFromViolation(method.getAnnotation(violation), isChar(method));
            }

            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Field getDeclaredField(Class clazz, String fieldName) {
        Field field = null;

        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
                return getDeclaredField(clazz.getSuperclass(), fieldName);
            }
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        return field;
    }

    public Method getDeclaredMethod(Class clazz, String fieldName) {
        Method method = null;

        try {
            method = clazz.getDeclaredMethod("get" + StringUtils.getUpperFirstLetter(fieldName));
        } catch (NoSuchMethodException ex) {
            if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
                return getDeclaredMethod(clazz.getSuperclass(), fieldName);
            }
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        return method;
    }

    public boolean isChar(Field field) {
        if (field.getType().equals(String.class) || field.getType().equals(CharSequence.class)) {
            return true;
        }
        return false;
    }

    public boolean isChar(Method method) {
        if (method.getReturnType().equals(String.class) || method.getReturnType().equals(CharSequence.class)) {
            return true;
        }
        return false;
    }

    public class AnnotationFromViolation {

        private Annotation annotation;
        private boolean forChar;

        public AnnotationFromViolation(Annotation annotation, boolean forChar) {
            this.annotation = annotation;
            this.forChar = forChar;
        }

        public Annotation getAnnotation() {
            return annotation;
        }

        public void setAnnotation(Annotation annotation) {
            this.annotation = annotation;
        }

        public boolean isForChar() {
            return forChar;
        }

        public void setForChar(boolean forChar) {
            this.forChar = forChar;
        }
    }
}
