import {Component, Input, OnInit} from '@angular/core';
import {Feature} from "../../entities/feature";

@Component({
  selector: 'app-feature-list',
  templateUrl: './feature-list.component.html',
  styleUrls: ['./feature-list.component.css']
})
export class FeatureListComponent implements OnInit {

  @Input() features: Feature[];
  @Input() featureCount: Map<string, number>;

  constructor() { }

  ngOnInit() {
  }

  getCountTooltipPart(featureId: string): string {
    if (this.featureCount && this.featureCount.has(featureId)) {
      return ', on ' + this.featureCount.get(featureId) + ' Nodes';
    }
    return '';
  }
}
