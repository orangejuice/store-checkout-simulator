# Supermarket Checkout Simulator


##### preference Panel functions
1. read/save preference.
2. general extent of busy.
    - very idle 5 comes per min /period 12s
    - idle 10 comes per min /period 6s
    - normal 20 comes per min /period 3s
    - busy 40 comes per min /period 1.5s
    - very busy 80 comes per min /period 0.75s

##### simulation Panel functions
1. play speed control
2. logs panel
3. process visualization.
4. normal/expressway support
5. real-time detail available (mouse hover) 
5. human-simulation: auto get the best channel logic
5. human-simulation: temper good/bad
5. processing bar
6. record:
    - total wait time for each customer
    - total utilization for each checkout
    - total products processed //TODO add
    - average customer wait time //TODO add
    - average checkout utilization //TODO add
    - average products per trolley //TODO add
    - the number of lost customers //TODO add
##### statistics Panel functions
1. selectable text.
2. data visualization.
    - pie chart - waitingFor<1mins/1-5/5-10/10-15/15-30/>30/lost customer
    - total wait time for each customer
    - the utilization of all checkouts(normal+expressway) by time
2. export to pdf report.

# remark

1. resolve the problem of, run in jdk11 and javafx11, without add the long long vm options in IntelliJ Idea:
`
FXMLLoaderHelper (in unnamed module @0xcb6a90) cannot access class com.sun.javafx.util.Utils (in module javafx.graphics) because module javafx.graphics does not export com.sun.javafx.util to unnamed module @0xcb6a90
`
refer to the 'module-info.java' file.
2. another problem. confusing deeply.
`
Exception in thread "JavaFX Application Thread" java.lang.IndexOutOfBoundsException: Index -1 out of bounds for length 4
	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
	at java.base/java.util.Objects.checkIndex(Objects.java:372)
	at java.base/java.util.ArrayList.get(ArrayList.java:458)
	at javafx.base/com.sun.javafx.collections.ObservableListWrapper.get(ObservableListWrapper.java:89)
	at javafx.base/com.sun.javafx.collections.VetoableListDecorator.get(VetoableListDecorator.java:306)
	at javafx.graphics/javafx.scene.Parent.updateCachedBounds(Parent.java:1701)
`
https://bugs.openjdk.java.net/browse/JDK-8163078
https://bugs.openjdk.java.net/browse/JDK-8198577


# Reference
1. https://melbournechapter.net/explore/grocery-clipart-customer-shopping/
2. https://github.com/cping/LGame/tree/master/dev-res
3. https://openjfx.io/openjfx-docs/#IDE-Intellij
4. http://aalmiray.github.io/ikonli/cheat-sheet-fontawesome5.html#_regular
5. http://aalmiray.github.io/ikonli/#_fontawesome5