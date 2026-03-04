import React from 'react';
import { Form } from 'react-bootstrap';
import Dp, { registerLocale } from 'react-datepicker';
import dayjs from 'dayjs';
import 'react-datepicker/dist/react-datepicker.css';

import es from 'date-fns/locale/es';
registerLocale('es', es);

export default function DatePicker({ value, setValue, maxDate, minDate, disabledDatesIntervals = [] }) {
    return (
        <>
            <Dp
                excludeDateIntervals={disabledDatesIntervals}
                locale="es"
                selected={value}
                onChange={setValue}
                dateFormat="dd/MM/yyyy"
                className="w-100 form-control"
                placeholderText="dd/mm/aaaa"
                maxDate={maxDate}
                minDate={minDate}
            />
        </>
    );
}
