import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LibraryRoutineAddComponent } from './library-routine-add.component';

describe('LibraryRoutineAddComponent', () => {
  let component: LibraryRoutineAddComponent;
  let fixture: ComponentFixture<LibraryRoutineAddComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LibraryRoutineAddComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LibraryRoutineAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
