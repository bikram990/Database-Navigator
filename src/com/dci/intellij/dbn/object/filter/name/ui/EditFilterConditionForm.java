package com.dci.intellij.dbn.object.filter.name.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.ui.ComboBoxSelectionKeyListener;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.name.CompoundFilterCondition;
import com.dci.intellij.dbn.object.filter.name.ConditionJoinType;
import com.dci.intellij.dbn.object.filter.name.ConditionOperator;
import com.dci.intellij.dbn.object.filter.name.SimpleFilterCondition;

public class EditFilterConditionForm extends DBNFormImpl<EditFilterConditionDialog> {
    private JPanel mainPanel;
    private JTextField textPatternTextField;
    private JLabel objectNameLabel;
    private DBNComboBox<ConditionOperator> operatorComboBox;
    private JLabel wildcardsHintLabel;
    private DBNComboBox<ConditionJoinType> joinTypeComboBox;

    private SimpleFilterCondition condition;
    public enum Operation {CREATE, EDIT, JOIN}

    public EditFilterConditionForm(EditFilterConditionDialog parentComponent, CompoundFilterCondition parentCondition, SimpleFilterCondition condition, DBObjectType objectType, Operation operation) {
        super(parentComponent);
        this.condition = condition == null ? new SimpleFilterCondition(ConditionOperator.EQUAL, "") : condition;
        joinTypeComboBox.setValues(ConditionJoinType.values());
        joinTypeComboBox.setVisible(false);
        if (operation == Operation.JOIN) {
            joinTypeComboBox.setVisible(true);
            if (parentCondition != null && parentCondition.getJoinType() == ConditionJoinType.AND) {
                joinTypeComboBox.setSelectedValue(ConditionJoinType.OR);
            } else {
                joinTypeComboBox.setSelectedValue(ConditionJoinType.AND);
            }
        } else if (operation == Operation.CREATE) {
            if (parentCondition != null && parentCondition.getConditions().size() == 1) {
                joinTypeComboBox.setVisible(true);
                joinTypeComboBox.setSelectedValue(ConditionJoinType.AND);
            }
        }

        objectNameLabel.setIcon(objectType.getIcon());
        objectNameLabel.setText(objectType.getName().toUpperCase() + "_NAME");

        operatorComboBox.setValues(ConditionOperator.values());;
        if (operation == Operation.EDIT) {
            textPatternTextField.setText(condition == null ? "" : condition.getText());
            operatorComboBox.setSelectedValue(condition == null ? null : condition.getOperator());

        }
        textPatternTextField.selectAll();
        textPatternTextField.setToolTipText("<html>While editing, <br> " +
                "press <b>Up/Down</b> keys to change the operator</html>");
        textPatternTextField.addKeyListener(ComboBoxSelectionKeyListener.create(operatorComboBox, false));

        //wildcardsHintLabel.setIcon(Icons.COMMON_INFO);
        //wildcardsHintLabel.setDisabledIcon(Icons.COMMON_INFO_DISABLED);
        showHideWildcardHint();

        operatorComboBox.addListener(new ValueSelectorListener<ConditionOperator>() {
            @Override
            public void selectionChanged(ConditionOperator oldValue, ConditionOperator newValue) {
                showHideWildcardHint();
            }
        });
    }

    private void showHideWildcardHint() {
        ConditionOperator operator = operatorComboBox.getSelectedValue();
        wildcardsHintLabel.setEnabled(operator != null && operator.allowsWildcards());
    }

    public JComponent getFocusComponent() {
        return textPatternTextField;
    }

    public SimpleFilterCondition getCondition() {
        condition.setOperator(operatorComboBox.getSelectedValue());
        condition.setText(textPatternTextField.getText().trim());
        return condition;
    }

    public ConditionJoinType getJoinType() {
        return joinTypeComboBox.getSelectedValue();
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
        condition = null;
    }
}
