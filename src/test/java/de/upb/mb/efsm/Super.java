package de.upb.mb.efsm;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
abstract class Super {
  private final String id;

  public Super(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
