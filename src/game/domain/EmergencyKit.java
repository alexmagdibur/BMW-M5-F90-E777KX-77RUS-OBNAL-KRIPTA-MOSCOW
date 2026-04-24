package domain;

public class EmergencyKit {

    private boolean firstAidKit;
    private boolean fireExtinguisher;
    private boolean warningTriangle;

    public EmergencyKit(boolean firstAidKit, boolean fireExtinguisher, boolean warningTriangle) {
        this.firstAidKit = firstAidKit;
        this.fireExtinguisher = fireExtinguisher;
        this.warningTriangle = warningTriangle;
    }

    public boolean hasFirstAidKit()     { return firstAidKit; }
    public boolean hasFireExtinguisher() { return fireExtinguisher; }
    public boolean hasWarningTriangle()  { return warningTriangle; }

    public void setFirstAidKit(boolean v)      { this.firstAidKit = v; }
    public void setFireExtinguisher(boolean v)  { this.fireExtinguisher = v; }
    public void setWarningTriangle(boolean v)   { this.warningTriangle = v; }

    public boolean isComplete() {
        return firstAidKit && fireExtinguisher && warningTriangle;
    }

    @Override
    public String toString() {
        return String.format("Аптечка=%s | Огнетушитель=%s | Знак=%s",
                firstAidKit ? "есть" : "нет",
                fireExtinguisher ? "есть" : "нет",
                warningTriangle ? "есть" : "нет");
    }
}
