import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LibraryOverviewComponent } from './library-overview.component';

describe('LibraryOverviewComponent', () => {
  let component: LibraryOverviewComponent;
  let fixture: ComponentFixture<LibraryOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LibraryOverviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LibraryOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
