import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LibraryRoutineDetailComponent } from './library-routine-detail.component';

describe('LibraryRoutineDetailComponent', () => {
  let component: LibraryRoutineDetailComponent;
  let fixture: ComponentFixture<LibraryRoutineDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LibraryRoutineDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LibraryRoutineDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
