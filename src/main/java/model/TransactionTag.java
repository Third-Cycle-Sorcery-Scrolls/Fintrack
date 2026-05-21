package model;

public class TransactionTag {
    private int transactionId;
    private int tagId;

    public TransactionTag() {
    }

    public TransactionTag(int transactionId, int tagId) {
        this.transactionId = transactionId;
        this.tagId = tagId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    @Override
    public String toString() {
        return "TransactionTag{" +
                "transactionId=" + transactionId +
                ", tagId=" + tagId +
                '}';
    }
}
