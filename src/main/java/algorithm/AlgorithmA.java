package algorithm;

/*
* Finding the most effective way to get from start to end
* Using openList and closedList
* OpenList contains states which are waiting for discovery
* ClosedList contains already found states to avoid discovering them again
* The beginning of algorithm will start with adding start state of our state, and then we'll choose
*       the cheapest way to continue exploring new states.
* When we move to another state we add it to our closed list.
* When we are on our new state we discover all the new possible ways to continue discovering next state.
* And just like that we continue to discover our final destination.
*/

public class AlgorithmA {
}
