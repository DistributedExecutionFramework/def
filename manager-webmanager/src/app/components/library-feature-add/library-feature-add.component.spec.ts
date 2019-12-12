import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LibraryFeatureAddComponent } from './library-feature-add.component';

describe('LibraryFeatureAddComponent', () => {
  let component: LibraryFeatureAddComponent;
  let fixture: ComponentFixture<LibraryFeatureAddComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LibraryFeatureAddComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LibraryFeatureAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
