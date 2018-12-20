package com.vince.retailmanager.web.exception;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

public class BindingErrorsResponse {


  private List<BindingError> bindingErrors = new ArrayList<>();

  public List<BindingError> getBindingErrors() {
    return bindingErrors;
  }

  public void setBindingErrors(List<BindingError> bindingErrors) {
    this.bindingErrors = bindingErrors;
  }

  public void addError(BindingError bindingError) {
    this.bindingErrors.add(bindingError);
  }

  public void addAllErrors(BindingResult bindingResult) {
    for (FieldError fieldError : bindingResult.getFieldErrors()) {
      BindingError error = new BindingError();
      error.setObjectName(fieldError.getObjectName());
      error.setFieldName(fieldError.getField());
      if (fieldError.getRejectedValue() != null) {
        error.setFieldValue(fieldError.getRejectedValue().toString());
      }
      error.setErrorMessage(fieldError.getDefaultMessage());
      addError(error);
    }
  }

  public String toJSON() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    String errorsAsJSON = "";
    try {
//            errorsAsJSON = mapper.writeValueAsString(bindingErrors);
      Map<String, Object> map = new HashMap<>();
      map.put("errors", bindingErrors);
      errorsAsJSON = mapper.writeValueAsString(map);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return errorsAsJSON;
  }

  @Override
  public String toString() {
    return "BindingErrorsResponse [bindingErrors=" + bindingErrors + "]";
  }

  protected class BindingError {

    private String objectName;
    private String fieldName;
    private String fieldValue;
    private String errorMessage;

    public BindingError() {
      this.objectName = "";
      this.fieldName = "";
      this.fieldValue = "";
      this.errorMessage = "";
    }

    protected String getObjectName() {
      return objectName;
    }

    protected void setObjectName(String objectName) {
      this.objectName = objectName;
    }

    protected String getFieldName() {
      return fieldName;
    }

    protected void setFieldName(String fieldName) {
      this.fieldName = fieldName;
    }

    protected String getFieldValue() {
      return fieldValue;
    }

    protected void setFieldValue(String fieldValue) {
      this.fieldValue = fieldValue;
    }

    protected String getErrorMessage() {
      return errorMessage;
    }

    protected void setErrorMessage(String error_message) {
      this.errorMessage = error_message;
    }

    @Override
    public String toString() {
      return "BindingError [objectName=" + objectName + ", fieldName=" + fieldName + ", fieldValue="
          + fieldValue
          + ", errorMessage=" + errorMessage + "]";
    }

  }

}
