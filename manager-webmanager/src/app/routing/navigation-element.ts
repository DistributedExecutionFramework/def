export class NavigationElement {
  private readonly name: string;
  private readonly route: string;
  private readonly tooltipText: string;

  constructor(name: string, route: string, tooltipText: string) {
    this.name = name;
    this.route = route;
    this.tooltipText = tooltipText;
  }

  getName(): string {
    return this.name;
  }

  getRoute(): string {
    return this.route;
  }

  getTooltipText(): string {
    return this.tooltipText;
  }
}
